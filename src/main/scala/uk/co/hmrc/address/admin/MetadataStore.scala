/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.co.hmrc.address.admin

import com.mongodb.casbah.Imports._
import uk.co.hmrc.address.services.mongo.CasbahMongoConnection
import uk.co.hmrc.logging.SimpleLogger


class MetadataStore(mongoDbConnection: CasbahMongoConnection, logger: SimpleLogger) {

  private val collection: MongoCollection = mongoDbConnection.getConfiguredDb("admin")

  val gbAddressBaseCollectionName = new MongoStoredMetadataItem(collection, "gbAddressBaseCollectionName", "addressbase_gb", logger)
  val niAddressBaseCollectionName = new MongoStoredMetadataItem(collection, "niAddressBaseCollectionName", "addressbase_ni", logger)
}


trait StoredMetadataItem {
  def get: String
}
