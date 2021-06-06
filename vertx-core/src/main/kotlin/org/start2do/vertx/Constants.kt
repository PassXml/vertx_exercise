/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get

/**
 * @Author passxml
 * @date 2020/9/1:10:22
 */
class Global(jsonObject: JsonObject) {
  val env: String = jsonObject.getString(ENV, ENV_DEV)
  val scanPackage = getArray(jsonObject.getJsonArray(SERVICE_SCAN_PACKAGES))
  val serviceAddress: String = jsonObject.getString(SERVICE_ADDRESS)
  val verticle = getArray(jsonObject.getJsonArray(VERTICLE))

  companion object {
    private fun getArray(array: JsonArray): Array<String> {
      val result = mutableListOf<String>()
      for (i in 0 until array.size()) {
        result.plusElement(array.getString(i))
      }
      return result.toTypedArray()
    }

    const val SYS: String = "[SYS]"
    const val ENV_DEV = "dev"

    const val ENV = "env"

    /**
     *  扫描路径
     */
    const val SERVICE_SCAN_PACKAGES = "serviceScanPackages"

    /**
     *  服务推送地址
     */
    const val SERVICE_ADDRESS = "serviceAddress"
    const val VERTICLE = "verticle"
  }
}

class EventBusSetting(jsonObject: JsonObject) {

  private val eventbusSetting = jsonObject.getJsonObject(MAIN, JsonObject())
  val enable = eventbusSetting.getBoolean(ENABLE, false)
  val haGroup = eventbusSetting.getString(HAGROUP, "vertx")
  val type = eventbusSetting.getString(TYPE)
  val host = eventbusSetting.getString(ENABLE, "127.0.0.1")
  val port = eventbusSetting.getInteger(PORT, 8000)
  val publicHost = eventbusSetting.getString(PUBLICHOST, host)
  val publicPort = eventbusSetting.getInteger(PUBLICPORT, port)
  val connectTimeout = eventbusSetting.getInteger(CONNECTTIMEOUT, 3000)

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
  }

  fun getSSLConfig(): SSL = SSL(eventbusSetting)
  fun getIgniteConfig(): IgniteConfig = IgniteConfig(eventbusSetting)
  fun getZookeeperConfig(): ZookeeperConfig {
    return ZookeeperConfig(eventbusSetting)
  }

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

  class IgniteConfig(jsonObject: JsonObject) {
    private val igniteConfig = jsonObject.getJsonObject(
      MAIN
    )
    val addresses = igniteConfig.getString(ADDRESSES)
    val dataEncryptionCertPath = igniteConfig.getString(DATAENCRYPTIONCERTPATH)
    val dataEncryptionPassword = igniteConfig.getString(DATAENCRYPTIONCERTPASSWORD)
    val workDir = igniteConfig.getString(WORKDIR)
    val certPath = igniteConfig.getString(CERTPATH)
    val certPassword = igniteConfig.getString(CERTPASSWORD)
    val caPath = igniteConfig.getString(CAPATH)
    val caPassword = igniteConfig.getString(CAPASSWORD)

    companion object {
      const val TYPE = "ignite"
      const val MAIN = "ignite_config"
      const val ADDRESSES = "addresses"
      const val DATAENCRYPTIONCERTPATH = "dataEncryptionCertPath"
      const val DATAENCRYPTIONCERTPASSWORD = "dataEncryptionPassword"
      const val WORKDIR = "workDir"
      const val CERTPATH = "certPath"
      const val CERTPASSWORD = "certPassword"
      const val CAPATH = "caPath"
      const val CAPASSWORD = "caPassword"
    }
  }

  class ZookeeperConfig(jsonObject: JsonObject) {
    val zookeeperConfig = jsonObject.getJsonObject(
      MAIN
    )
    val zookeeperHosts = zookeeperConfig.getString(ZOOKEEPERHOSTS)
    val rootPath = zookeeperConfig.getString(ROOTPATH)

    companion object {
      const val TYPE = "zookeeper"
      const val MAIN = "zookeeperConfig"
      const val ZOOKEEPERHOSTS = "zookeeperHosts"
      const val ROOTPATH = "rootPath"
    }

    class Retry(jsonObject: JsonObject) {
      private val config = jsonObject.getJsonObject(EventBusSetting.MAIN, JsonObject()).getJsonObject(
        MAIN, JsonObject()
      ).getJsonObject(MAIN)
      val initialSleepTime = config.getInteger(INITIALSLEEPTIME)
      val maxTimes = config.getInteger(MAXTIMES)

      companion object {
        const val MAIN = "retry"
        const val INITIALSLEEPTIME = "initialSleepTime"
        const val MAXTIMES = "maxTimes"
      }
    }
  }
}
