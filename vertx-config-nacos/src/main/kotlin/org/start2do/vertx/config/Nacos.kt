package org.start2do.vertx.config

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineExceptionHandler
import org.start2do.vertx.GetNetworkSetting
import org.start2do.vertx.ext.createFuture
import org.start2do.vertx.ext.getLogger
import java.io.Closeable

/**
 * @Author Lijie
 * @date  2021/12/4:14:23
 */
class Nacos : GetNetworkSetting, Closeable {
  private val log = getLogger(Nacos::class.java)
  private lateinit var client: WebClient
  private lateinit var config: NetWorkSetting
  private lateinit var accessToken: String
  private val timeOut = 5000
  private suspend fun login() {
    if (config.username.isNullOrBlank() && config.password.isNullOrBlank()) {
      return
    }
    val response = client.post(config.port, config.host, "/nacos/v1/auth/login")
      .sendBuffer(Buffer.buffer("username=${config.username}&password=${config.password}")).await()
    accessToken = response.bodyAsJsonObject().getString("accessToken")
  }

  private suspend fun getConfig(): JsonObject {
    log.debug(config)
    val body = client.get(config.port, config.host, "/nacos/v1/cs/configs").addQueryParam(
      "dataId", config.flag
    ).addQueryParam(
      "group", config.groupName ?: "DEFAULT_GROUP"
    ).timeout(timeOut.toLong()).send().await().body()
    return body.toJsonObject()
  }

  override fun close() {
    client.close()
  }

  override fun get(vertx: Vertx, config: NetWorkSetting): Future<JsonObject> {
    client = WebClient.create(vertx, WebClientOptions().setConnectTimeout(timeOut));
    this.config = config;
    return createFuture {
      login()
      getConfig()
    }
  }
}
