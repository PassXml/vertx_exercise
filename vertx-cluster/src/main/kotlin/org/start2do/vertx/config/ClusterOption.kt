package org.start2do.vertx.config

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.events.EventType
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.spi.encryption.keystore.KeystoreEncryptionSpi
import org.start2do.utils.ResourceUtil
import org.start2do.vertx.EventBusSetting
import org.start2do.vertx.ignite.MySSLFactory


/**
 * @Author Lijie
 * @date  2021/12/12:09:03
 */
object ClusterOption {
  @JvmStatic
  fun file(path: String): ClusterManager? {
    val jsonConfig =
      JsonObject(Buffer.buffer(ResourceUtil.getFile(path)!!.readBytes())).getJsonObject(EventBusSetting.MAIN)
        ?: return null
    return when (jsonConfig.getString(jsonConfig.getString(EventBusSetting.TYPE), "")) {
      IgniteConfig.MAIN -> {
        IgniteConfig.get(jsonConfig)
      }
      ZookeeperConfig.MAIN -> {
        ZookeeperConfig.get(jsonConfig)
      }
      else -> {
        return null
      }
    }
  }

  @JvmStatic
  fun withIgniteOption(option: IgniteConfig): ClusterManager {
    return option.get()
  }

  @JvmStatic
  fun withZookeeperConfig(option: ZookeeperConfig): ClusterManager {
    return option.get()
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
  fun get(): IgniteClusterManager {
    var config = IgniteConfiguration()
    val tcpDiscoverySpi = TcpDiscoverySpi()
    config.setIncludeEventTypes(
      EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_REMOVED
    )
    config.gridLogger = io.vertx.spi.cluster.ignite.impl.VertxLogger()
    tcpDiscoverySpi.localPort = 48500
    tcpDiscoverySpi.localPortRange = 20
    val busAddress = this.addresses

    /** 设置集群地址 */
    val tcpDiscoveryMulticastIpFinder = TcpDiscoveryMulticastIpFinder()
    if (busAddress != null) {
      tcpDiscoveryMulticastIpFinder.setAddresses(busAddress.map { it.toString() })
    }
    tcpDiscoverySpi.ipFinder = tcpDiscoveryMulticastIpFinder
    val dataCert = this.dataEncryptionCertPath
    val dataPassword = this.dataEncryptionPassword
    if (dataCert.isNotEmpty() && dataPassword.isNotEmpty()) {
      val encSpi = KeystoreEncryptionSpi()
      encSpi.keyStorePath = dataCert
      encSpi.setKeyStorePassword(dataPassword.toCharArray())
      config.encryptionSpi = encSpi
    }
    val clientCert = this.certPath
    val clientPassword = this.certPassword
    val caCert = this.caPath
    val caPassword = this.caPassword
    if (!clientCert.isNullOrEmpty() && !clientPassword.isNullOrEmpty()) {
      config.sslContextFactory = MySSLFactory(
        clientCert, clientPassword, caCert, caPassword
      )
    }
    val workDir = this.workDir
    if (!workDir.isNullOrEmpty()) {
      config.workDirectory = workDir
    }
    config.discoverySpi = tcpDiscoverySpi
    return IgniteClusterManager(config)
  }

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

    fun get(jsonConfig: JsonObject): ClusterManager {
      return IgniteConfig(jsonConfig).get()
    }
  }
}

class ZookeeperConfig(jsonObject: JsonObject) {
  val zookeeperConfig = jsonObject.getJsonObject(
    MAIN
  )
  private val zookeeperHosts: String = zookeeperConfig.getString(ZOOKEEPERHOSTS)!!
  private val rootPath: String = zookeeperConfig.getString(ROOTPATH)
  fun get(): ClusterManager {
    return ZookeeperClusterManager(
      JsonObject().put(
        ZOOKEEPERHOSTS, this.zookeeperHosts
      ).put(
        ROOTPATH, this.rootPath
      )
    )
  }

  companion object {
    fun get(jsonConfig: JsonObject): ClusterManager {
      return ZookeeperClusterManager(jsonConfig.getJsonObject(MAIN))
    }

    const val TYPE = "zookeeper"
    const val MAIN = "zookeeperConfig"
    const val ZOOKEEPERHOSTS = "zookeeperHosts"
    const val ROOTPATH = "rootPath"
  }

  class Retry(jsonObject: JsonObject) {
    private val config = jsonObject.getJsonObject(EventBusSetting.MAIN, JsonObject()).getJsonObject(
      MAIN, JsonObject()
    ).getJsonObject(MAIN)
    private val initialSleepTime = config.getInteger(INITIALSLEEPTIME)
    private val maxTimes = config.getInteger(MAXTIMES)

    companion object {
      const val MAIN = "retry"
      const val INITIALSLEEPTIME = "initialSleepTime"
      const val MAXTIMES = "maxTimes"
    }
  }
}

enum class ClusterType {
  ZOOKEEPER, IGNITE
}
