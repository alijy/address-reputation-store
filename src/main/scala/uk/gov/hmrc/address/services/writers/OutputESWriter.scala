/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.address.services.writers

import java.util.Date

import com.sksamuel.elastic4s._
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse
import uk.gov.hmrc.BuildProvenance
import uk.gov.hmrc.address.osgb.DbAddress
import uk.gov.hmrc.address.services.es.{ESSchema, IndexMetadata, IndexState}
import uk.gov.hmrc.logging.SimpleLogger

import scala.collection.immutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class OutputESWriter(model: IndexState, statusLogger: SimpleLogger, indexMetadata: IndexMetadata,
                     settings: WriterSettings, ec: ExecutionContext, provenance: BuildProvenance) extends OutputWriter with ElasticDsl {

  private implicit val x = ec
  private val address = IndexMetadata.address
  private val indexName = model.formattedName
  private var hasFailed = false

  override def existingTargetThatIsNewerThan(date: Date): Option[String] = {
    val similar = indexMetadata.existingIndexNamesLike(model)
    val found = similar.reverse.find {
      name =>
        val info = indexMetadata.findMetadata(name)
        info.exists(_.completedAfter(date))
    }
    found.map(_.formattedName)
  }

  override def begin() {
    indexMetadata.deleteIndexIfExists(model)

    indexMetadata.clients foreach { client =>
      client execute {
        ESSchema.createIndexDefinition(indexName, address,
          ESSchema.Settings(indexMetadata.numShards(model.productName), 0, "60s"))
      } await()
      Unit
    }
    indexMetadata.writeIngestSettingsTo(model, settings, provenance)
  }

  override def output(a: DbAddress) {
    val at = a.forElasticsearch
    addBulk(
      index into indexName -> address fields at id a.id routing a.postcode
    )
  }

  override def end(completed: Boolean): Boolean = {
    if (bulkCount != 0) {
      val fbrs = indexMetadata.clients map { client =>
        client execute {
          bulk(
            bulkStatements
          )
        }
      }

      awaitBulkResultsAndCheckFailures(fbrs)
      hasFailed
    }

    // we have finished! let's celebrate
    if (completed) {
      val fuss = indexMetadata.clients map { client =>
        client execute {
          update settings indexName set Map(
            "index.refresh_interval" -> "1s"
          )
        }
      }
      Future.sequence(fuss).await(Duration.Inf)
      indexMetadata.writeCompletionDateTo(model)
    }

    statusLogger.info(s"Finished ingesting to index $indexName")
    hasFailed
  }

  private var bulkCount = 0
  private var bulkStatements = collection.mutable.Buffer[IndexDefinition]()

  private def addBulk(i: IndexDefinition) {
    bulkStatements += i
    bulkCount += 1

    if (bulkCount >= settings.bulkSize) {
      val fbrs = indexMetadata.clients map { client =>
        client execute {
          bulk(
            bulkStatements
          )
        }
      }

      awaitBulkResultsAndCheckFailures(fbrs)

      bulkCount = 0
      bulkStatements.clear()
      Thread.sleep(settings.loopDelay)
    }
  }

  private def awaitBulkResultsAndCheckFailures(fbrs: Seq[Future[BulkResult]]) {
    val bulkResults = Future.sequence(fbrs).await(Duration.Inf)

    val failures = bulkResults.filter(_.hasFailures)

    failures.foreach {
      br =>
        statusLogger.warn(s"Elasticsearch failure processing bulk insertion - ${br.failureMessage}")
        hasFailed = true
        throw new Exception(s"Elasticsearch failure processing bulk insertion - ${br.failureMessage}")
    }
  }
}
