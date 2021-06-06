/*  大道至简 (C)2021 */
package org.start2do.vertx.db

import io.vertx.core.json.JsonObject

/**
 * @Author Lijie
 * @date 2021/5/16:10:45
 */
class DbSetting(jsonObject: JsonObject) {
  private val dbSetting = jsonObject.getJsonObject(MAIN)
  val type = dbSetting.getString(TYPE)
  val host = dbSetting.getString(HOST)
  val port = dbSetting.getInteger(PORT)
  val username = dbSetting.getString(USERNAME)
  val password = dbSetting.getString(PASSWORD)
  val database = dbSetting.getString(DATABASE)

  companion object {
    const val MAIN = "dbSetting"
    const val TYPE = "type"
    const val HOST = "host"
    const val PORT = "port"
    const val USERNAME = "username"
    const val PASSWORD = "password"
    const val DATABASE = "database"
  }

  fun getFlyWaySetting() = Flyway(dbSetting)
  class Flyway(jsonObject: JsonObject) {
    private val flywaySetting = jsonObject.getJsonObject(MAIN, JsonObject())
    val enable = flywaySetting.getBoolean(ENABLE, false)
    val mode = flywaySetting.getString(MODE)

    companion object {
      const val MAIN = "flyway"
      const val ENABLE = "enable"
      const val MODE = "mode"
    }
  }
}
