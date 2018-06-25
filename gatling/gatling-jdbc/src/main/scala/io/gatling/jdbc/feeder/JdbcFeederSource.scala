/**
 * Copyright 2011-2015 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.jdbc.feeder

import java.sql.DriverManager
import java.sql.ResultSet.{ CONCUR_READ_ONLY, TYPE_FORWARD_ONLY }
import scala.annotation.tailrec
import scala.collection.breakOut

import io.gatling.commons.util.Io._
import io.gatling.core.feeder.Record

object JdbcFeederSource {

  def apply(url: String, username: String, password: String, sql: String): Vector[Record[Any]] =
    withCloseable(DriverManager.getConnection(url, username, password)) { connection =>
      val preparedStatement = connection.prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
      val resultSet = preparedStatement.executeQuery
      val metadata = resultSet.getMetaData
      val columnCount = metadata.getColumnCount

      val columnLabels = for (i <- 1 to columnCount) yield metadata.getColumnLabel(i)

        def computeRecord: Record[Any] =
          (for (i <- 1 to columnCount) yield columnLabels(i - 1) -> resultSet.getObject(i))(breakOut)

        @tailrec
        def loadRec(records: Vector[Record[Any]]): Vector[Record[Any]] =
          if (!resultSet.next) records
          else loadRec(records :+ computeRecord)

      loadRec(Vector.empty)
    }
}
