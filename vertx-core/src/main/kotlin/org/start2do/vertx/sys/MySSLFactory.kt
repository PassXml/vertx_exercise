/*  大道至简 (C)2020 */
package org.start2do.vertx.sys

import org.start2do.vertx.utils.ConfigFileReadUtils
import java.security.KeyStore
import javax.cache.configuration.Factory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

/**
 * @Author passxml
 * @date 2020/7/30:16:17
 */
class MySSLFactory(
  private val keyStoreFilePath: String,
  private val keyStorePassword: String,
  private val trustStoreFilePath: String,
  private val trustStorePassword: String
) : Factory<SSLContext> {
  override fun create(): SSLContext {
    val keyMgrFactory = KeyManagerFactory.getInstance("SunX509")
    val keyStore = KeyStore.getInstance("JKS")
    val charArray = keyStorePassword.toCharArray()
    keyStore.load(ConfigFileReadUtils.read(keyStoreFilePath), charArray)
    keyMgrFactory.init(keyStore, charArray)

    var mgrs: Array<TrustManager?>
    val trustMgrFactory = TrustManagerFactory.getInstance("SunX509")

    val trustStore: KeyStore = KeyStore.getInstance("JKS")
    trustStore.load(ConfigFileReadUtils.read(trustStoreFilePath), trustStorePassword.toCharArray())
    trustMgrFactory.init(trustStore)
    mgrs = trustMgrFactory.trustManagers
    var ctx = SSLContext.getInstance("TLS")
    ctx.init(keyMgrFactory.keyManagers, mgrs, null)
    return ctx
  }
}
