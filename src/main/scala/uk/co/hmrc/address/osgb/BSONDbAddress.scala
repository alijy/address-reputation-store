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

package uk.co.hmrc.address.osgb

import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

object BSONDbAddress extends BSONDocumentReader[DbAddress] {

  def read(bson: BSONDocument): DbAddress = {
    val id = bson.getAs[String]("_id").get
    val lines = bson.getAs[List[String]]("lines")
    val town = bson.getAs[String]("town").get
    val postcode = bson.getAs[String]("postcode").get

    if (lines.isDefined) {
      new DbAddress(id, lines.get, town, postcode)

    } else {
      // backward compatibility
      val line1 = bson.getAs[String]("line1").get
      val line2 = bson.getAs[String]("line2").get
      val line3 = bson.getAs[String]("line3").get
      DbAddress(id, line1, line2, line3, town, postcode)
    }
  }
}
