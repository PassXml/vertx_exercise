package org.start2do.vertx.config

import io.vertx.core.json.JsonObject

data class NetWorkSetting(
  val className: String,
  val host: String,
  val port: Int,
  var groupName: String? = null,
  var flag: String? = null,
  val path: String? = null,
  val username: String? = null,
  val password: String? = null,
  val certPath: String? = null,
  val caPath: String? = null
) {
  companion object {
    private const val CONFIG = "config"
    private const val CLASSNAME = "class_name"
    private const val HOST = "host"
    private const val PORT = "port"
    private const val PATH = "path"
    private const val USERNAME = "username"
    private const val PASSWORD = "password"
    private const val CERTPATH = "cert_path"
    private const val CAPATH = "ca_path"
    private const val GROUPNAME = "group_name"
    private const val FLAG = "flag"

    fun from(json: JsonObject): NetWorkSetting? {
      val jsonObject = json.getJsonObject(CONFIG) ?: return null
      return NetWorkSetting(
        jsonObject.getString(CLASSNAME),
        jsonObject.getString(HOST),
        jsonObject.getInteger(PORT),
        jsonObject.getString(PATH),
        jsonObject.getString(GROUPNAME),
        jsonObject.getString(FLAG),
        jsonObject.getString(USERNAME),
        jsonObject.getString(PASSWORD),
        jsonObject.getString(CERTPATH),
        jsonObject.getString(CAPATH)
      )
    }
  }
}
