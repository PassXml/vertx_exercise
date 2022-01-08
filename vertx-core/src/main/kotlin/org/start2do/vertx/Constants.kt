/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get

/**
 * @Author passxml
 * @date 2020/9/1:10:22
 */
class Global(jsonObject: JsonObject) {
  val env: String = jsonObject.getString(ENV, ENV_DEV)
  val serviceAddress: String = jsonObject.getString(SERVICE_ADDRESS, "service")
  val verticleDeployment = jsonObject.getJsonObject(VERTICLEDEPLOYMENT, JsonObject())
  private fun getArray(array: JsonArray): Array<String> {
    return array.map { it.toString() }.toTypedArray()
  }

  companion object {


    const val SYS: String = "[SYS]"
    const val ENV_DEV = "dev"

    const val ENV = "env"

    /**
     *  服务推送地址
     */
    const val SERVICE_ADDRESS = "serviceAddress"
    const val VERTICLEDEPLOYMENT = "verticleDeployment"
  }
}

class EventBusSetting(jsonObject: JsonObject) {

  private val eventbusSetting = jsonObject.getJsonObject(MAIN, JsonObject())
  val enable = eventbusSetting.getBoolean(ENABLE, false)
  val haGroup = eventbusSetting.getString(HAGROUP, "vertx")
  val type = eventbusSetting.getString(TYPE)
  val host = eventbusSetting.getString(HOST, "127.0.0.1")
  val port = eventbusSetting.getInteger(PORT, 8000)
  val publicHost = eventbusSetting.getString(PUBLICHOST, host)
  val publicPort = eventbusSetting.getInteger(PUBLICPORT, port)
  val connectTimeout = eventbusSetting.getInteger(CONNECTTIMEOUT, 3000)
  val eventLoopPoolSize = eventbusSetting.getInteger(EVENTLOOPPOOLSIZE, VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE)

  companion object {
    const val MAIN = "eventBus"
    const val ENABLE = "enable"
    const val HAGROUP = "haGroup"
    const val TYPE = "type"
    const val HOST = "host"
    const val PUBLICHOST = "publicHost"
    const val PORT = "port"
    const val PUBLICPORT = "publicPort"
    const val CONNECTTIMEOUT = "connectTimeout"
    const val EVENTLOOPPOOLSIZE = "eventLoopPoolSize"
  }

  fun getSSLConfig(): SSL = SSL(eventbusSetting)

  class SSL(jsonObject: JsonObject) {
    private val sslSetting = jsonObject.getJsonObject(MAIN, JsonObject())
    val enable = sslSetting.getBoolean(ENABLE, false)
    val certPath = sslSetting.getString(CERTPATH)
    val certPassword = sslSetting.getString(CERTPASSWORD)
    val caPath = sslSetting.getString(CAPATH)

    companion object {
      const val MAIN = "ssl"
      const val ENABLE = "enable"
      const val CERTPATH = "certPath"
      const val CERTPASSWORD = "certPassword"
      const val CAPATH = "caPath"
    }
  }


}
