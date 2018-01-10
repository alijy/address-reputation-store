/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.util._

// This captures the behaviour of locations in Elasticsearch, namely that they are expressed internally as
// a string in the form "lat,long".
//
// Note that Elasticsearch coordinates are spherical angles referenced to WGS84.

case class LatLong(lat: Double, long: Double) {
  def toLocation = lat.toString + "," + long.toString
}

object LatLong {
  def apply(location: Option[String]): Option[LatLong] = {
    if (location.isDefined) {
      val a = location.get.divide(',')
      Some(LatLong(a(0).toDouble, a(1).toDouble))
    } else None
  }
}
