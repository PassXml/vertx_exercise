package org.start2do.vertx.mongo

import io.vertx.core.json.JsonObject

/**
 * @Author Lijie
 * @date  2021/10/26:17:22
 */
class MongoDbSetting(jsonObject: JsonObject) {
  private val setting = jsonObject.getJsonObject(MAIN)
  val dbName = setting.getString(DBNAME)
  val connectionString = setting.getString(CONNECTIONSTRING)

  companion object {
    const val MAIN = "mongodb"
    const val DBNAME = "db_name"
    const val CONNECTIONSTRING = "connection_string"
  }
}
