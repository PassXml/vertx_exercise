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
import org.start2do.vertx.*
import org.start2do.vertx.Top.TopClusterManager
import org.start2do.vertx.utils.ConfigFileReadUtil
import org.start2do.vertx.utils.JacksonFix
import java.util.*

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
    setVertxOption(option, eventBusSetting)
    if (eventBusSetting.enable) {
      logger.info("启用集群总线")
      val clusterType = eventBusSetting.type
      logger.info("集群管理器为:{}", clusterType)
      initEventBus(option, eventBusSetting)
      option.clusterManager?.let {
        TopClusterManager = it
      }
    }
  }

  private fun setVertxOption(options: VertxOptions, eventBusSetting: EventBusSetting) {
    options.eventLoopPoolSize = eventBusSetting.eventLoopPoolSize
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
                ConfigFileReadUtil.read(busClientCertPath).readAllBytes()
              )
            )
            .setPassword(sslConfig.certPassword)
        )
        .setPemTrustOptions(
          PemTrustOptions().addCertValue(
            Buffer.buffer(
              ConfigFileReadUtil.read(sslConfig.caPath).readAllBytes()
            )
          )
        )
        .clientAuth = ClientAuth.REQUIRED
    }
  }
}
