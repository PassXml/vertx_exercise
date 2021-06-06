/*  大道至简 (C)2020 */
package org.start2do.vertx.config

import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystemOptions
import io.vertx.core.http.ClientAuth
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.net.PemTrustOptions
import io.vertx.core.net.PfxOptions
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.events.EventType
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.spi.encryption.keystore.KeystoreEncryptionSpi
import org.start2do.vertx.*
import org.start2do.vertx.Top.TopClusterManager
import org.start2do.vertx.sys.MySSLFactory
import org.start2do.vertx.utils.ConfigFileReadUtils
import org.start2do.vertx.utils.JacksonFix

object VertxOptionBuilder {
  private val logger = Log4j2LogDelegateFactory().createDelegate(Global.SYS)

  fun build(option: VertxOptions, config: JsonObject) {
    option.fileSystemOptions = FileSystemOptions().setFileCachingEnabled(false)
    JacksonFix.fixVertxJson()
    System.setProperty(
      "vertx.logger-delegate-factory-class-name",
      "io.vertx.core.logging.Log4j2LogDelegateFactory"
    )
    val eventBusSetting = EventBusSetting(config)
    if (eventBusSetting.enable) {
      logger.info("启用集群总线")
      val clusterType = eventBusSetting.type
      logger.info("集群管理器为:{}", clusterType)
      initEventBus(option, eventBusSetting)
      when (clusterType.toUpperCase()) {
        EventBusSetting.IgniteConfig.TYPE.toUpperCase() -> {
          initIgniteConfig(option, eventBusSetting.getIgniteConfig())
        }
        EventBusSetting.ZookeeperConfig.TYPE.toUpperCase() -> {
          initZookeeper(option, eventBusSetting.getZookeeperConfig())
        }
        else -> {
          throw Exception("无效的类型")
        }
      }
      option.clusterManager?.let {
        TopClusterManager = it
      }
    }
  }

  private fun initZookeeper(options: VertxOptions, configJson: EventBusSetting.ZookeeperConfig) {
    val clusterManager = ZookeeperClusterManager(
      configJson.zookeeperConfig
    )
    options.clusterManager = clusterManager
  }

  /** 初始化 */
  private fun initIgniteConfig(options: VertxOptions, igniteConfig: EventBusSetting.IgniteConfig) {
    var config = IgniteConfiguration()
    val tcpDiscoverySpi = TcpDiscoverySpi()
    config.setIncludeEventTypes(
      EventType.EVT_CACHE_OBJECT_PUT,
      EventType.EVT_CACHE_OBJECT_REMOVED
    )
    config.gridLogger = io.vertx.spi.cluster.ignite.impl.VertxLogger()
    tcpDiscoverySpi.localPort = 48500
    tcpDiscoverySpi.localPortRange = 20
    val busAddress = igniteConfig.addresses

    /** 设置集群地址 */
    val tcpDiscoveryMulticastIpFinder = TcpDiscoveryMulticastIpFinder()
    if (busAddress != null) {
      tcpDiscoveryMulticastIpFinder.setAddresses(
        busAddress.map { it.toString() }
      )
    }
    tcpDiscoverySpi.ipFinder = tcpDiscoveryMulticastIpFinder
    val dataCert = igniteConfig.dataEncryptionCertPath
    val dataPassword = igniteConfig.dataEncryptionPassword
    if (dataCert.isNotEmpty() && dataPassword.isNotEmpty()) {
      val encSpi = KeystoreEncryptionSpi()
      encSpi.keyStorePath = dataCert
      encSpi.setKeyStorePassword(dataPassword.toCharArray())
      config.encryptionSpi = encSpi
    }
    val clientCert = igniteConfig.certPath
    val clientPassword = igniteConfig.certPassword
    val caCert = igniteConfig.caPath
    val caPassword = igniteConfig.caPassword
    if (!clientCert.isNullOrEmpty() && !clientPassword.isNullOrEmpty()) {
      config.sslContextFactory = MySSLFactory(
        clientCert,
        clientPassword,
        caCert,
        caPassword
      )
    }
    val workDir = igniteConfig.workDir
    if (!workDir.isNullOrEmpty()) {
      config.workDirectory = workDir
    }
    config.discoverySpi = tcpDiscoverySpi
    val igniteClusterManager = IgniteClusterManager(config)
    options.clusterManager = igniteClusterManager
  }

  /** 初始化总线 */
  private fun initEventBus(options: VertxOptions, config: EventBusSetting) {

    options.setHAEnabled(true).haGroup = config.haGroup
    val busOptions = options.eventBusOptions
    busOptions.host = config.host
    busOptions.port = config.port
    busOptions.clusterPublicHost = config.publicHost
    busOptions.clusterPublicPort = config.publicPort
    busOptions.connectTimeout = config.connectTimeout
    // 设置SSL
    val sslConfig = config.getSSLConfig()
    busOptions.isSsl = sslConfig.enable
    if (busOptions.isSsl) {
      logger.info("设置SSL")
      /** 设置p12证书 */
      val busClientCertPath = sslConfig.certPath
      busOptions
        .setTrustAll(true)
        .setUseAlpn(true)
        .setPfxKeyCertOptions(
          PfxOptions()
            .setValue(
              Buffer.buffer(
                ConfigFileReadUtils.read(busClientCertPath).readAllBytes()
              )
            )
            .setPassword(sslConfig.certPassword)
        )
        .setPemTrustOptions(
          PemTrustOptions().addCertValue(
            Buffer.buffer(
              ConfigFileReadUtils.read(sslConfig.caPath).readAllBytes()
            )
          )
        )
        .clientAuth = ClientAuth.REQUIRED
    }
  }
}
