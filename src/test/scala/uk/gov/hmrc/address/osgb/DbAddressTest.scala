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

package uk.gov.hmrc.address.osgb

import org.scalatest.FunSuite
import uk.gov.hmrc.address.uk.Postcode

class DbAddressTest extends FunSuite {
  import DbAddress._

  val a1 = DbAddress("GB47070784", List("Line1", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8))
  val a2 = DbAddress("GB47070784", List("Line1", "Line2"), Some("Tynemouth"), "NE30 4HG", None, None, Some(1234), Some(English), None, None, None)
  val a4 = DbAddress("GB47070784", List("Line1"), None, "NE30 4HG", None, None, None, None, None, None, None)

  test("linesContainIgnoreCase - check we can find a match in line1, case insensitive") {
    assert(a1.linesContainIgnoreCase("e1") === true)
    assert(a1.linesContainIgnoreCase("E1") === true)
  }

  test("linesContainIgnoreCase - check we can find a match in line2, case insensitive") {
    assert(a1.linesContainIgnoreCase("e2") === true)
    assert(a1.linesContainIgnoreCase("E2") === true)
  }

  test("linesContainIgnoreCase - check we can find a match in line3, case insensitive") {
    assert(a1.linesContainIgnoreCase("e3") === true)
    assert(a1.linesContainIgnoreCase("E3") === true)
  }

  test("linesContainIgnoreCase - unmatched string should lead to the address being rejected") {
    assert(a1.linesContainIgnoreCase("SOMETHING") === false)
  }

  test("tupled") {
    assert(a1.forMongoDb.toMap === Map("_id" -> "GB47070784", "lines" -> List("Line1", "Line2", "Line3"), "town" -> "Tynemouth", "postcode" -> "NE30 4HG", "subdivision" -> "GB-ENG", "country" -> "UK", "localCustodianCode" -> 1234, "language" -> "en", "blpuState" -> 2, "logicalState" -> 1, "streetClass" -> 8))
    assert(a2.forMongoDb.toMap === Map("_id" -> "GB47070784", "lines" -> List("Line1", "Line2"), "town" -> "Tynemouth", "postcode" -> "NE30 4HG", "localCustodianCode" -> 1234, "language" -> "en"))
    assert(a4.forMongoDb.toMap === Map("_id" -> "GB47070784", "lines" -> List("Line1"), "postcode" -> "NE30 4HG"))
  }

  test("tupledFlat") {
    assert(a1.forElasticsearch === Map("id" -> "GB47070784", "line1" -> "Line1", "line2" -> "Line2", "line3" -> "Line3", "town" -> "Tynemouth", "postcode" -> "NE30 4HG", "subdivision" -> "GB-ENG", "country" -> "UK", "localCustodianCode" -> 1234, "language" -> "en", "blpuState" -> 2, "logicalState" -> 1, "streetClass" -> 8))
    assert(a2.forElasticsearch === Map("id" -> "GB47070784", "line1" -> "Line1", "line2" -> "Line2", "town" -> "Tynemouth", "postcode" -> "NE30 4HG", "localCustodianCode" -> 1234, "language" -> "en"))
    assert(a4.forElasticsearch === Map("id" -> "GB47070784", "line1" -> "Line1", "postcode" -> "NE30 4HG"))
  }

  test("uprn") {
    assert(a1.uprn === 47070784L)
    assert(a2.uprn === 47070784L)
    assert(a4.uprn === 47070784L)
  }

  test("splitPostcode") {
    assert(a1.splitPostcode === Postcode("NE", "30", "4", "HG"))
  }
}