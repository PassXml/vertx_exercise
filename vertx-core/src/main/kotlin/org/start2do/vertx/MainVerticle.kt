/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.spi.cluster.NodeListener
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.start2do.vertx.Top.Breaker
import org.start2do.vertx.Top.LongTimeExecutor
import org.start2do.vertx.Top.TopClusterManager
import org.start2do.vertx.utils.ConfigFileReadUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @Author passxml
 * @date 2020/9/3:17:12
 */
open class MainVerticle : AbstractVerticle() {

  companion object {
    val logger = Log4j2LogDelegateFactory().createDelegate(Global.SYS)
  }

  private val IDS = ConcurrentHashMap<String, String>()

  override fun init(vertx: Vertx, context: Context) {
    super.init(vertx, context)
    Runtime.getRuntime().addShutdownHook(object : Thread() {
      override fun run() {
        IDS.forEach {
          runBlocking {
            vertx.undeploy(it.key).await()
          }
        }
        vertx.close()
      }
    })
  }

  override fun stop(stopPromise: Promise<Void>?) {
    Breaker.close()
    val promise = Promise.promise<Void>()
    TopClusterManager?.leave(promise)
    super.stop(stopPromise)
  }

  open fun customInit() {
  }

  override fun start() {
    LongTimeExecutor = vertx.createSharedWorkerExecutor("Long_Time_Block", 5, 10, TimeUnit.MINUTES)
    Breaker = CircuitBreaker.create(
      "Main", vertx,
      CircuitBreakerOptions()
        .setMaxFailures(3)
        .setTimeout(5000)
        .setFallbackOnFailure(true)
    )
    // 更新
    TopClusterManager?.nodeListener(object : NodeListener {
      override fun nodeAdded(nodeID: String?) {
        logger.info("节点:${nodeID}加入")
      }

      override fun nodeLeft(nodeID: String?) {
        logger.info("节点:${nodeID}离开")
      }
    })
    vertx.exceptionHandler {
      logger.error(it.message, it)
    }
    val retriever: ConfigRetriever = ConfigRetriever.create(
      vertx,
      ConfigRetrieverOptions()
        .addStore(
          ConfigStoreOptions()
            .setType("env")
        )
    )
    customInit()
    val json = config()
      .mergeIn(JsonObject(Buffer.buffer(ConfigFileReadUtils.read("verticle.json").readBytes())))
      .mergeIn(JsonObject(Buffer.buffer(ConfigFileReadUtils.read("config.json").readBytes())))
    retriever.getConfig {
      if (!it.succeeded()) {
        logger.error(it.cause(), it.cause())
      }
      val mergeInJson = json.mergeIn(it.result())
      val global = Global(json)
      logger.info("Vert.x是否为集群模式:{}", vertx.isClustered)
      global.verticle.forEach { str ->
        vertx.deployVerticle(str, DeploymentOptions().setConfig(mergeInJson)) { asyncResult ->
          if (asyncResult.succeeded()) {
            logger.info("服务:${str}部署完成,ID:${asyncResult.result()}")
            IDS[asyncResult.result()] = asyncResult.result()
          } else {
            logger.error("服务:${str}部署失败")
            logger.error(asyncResult.cause(), asyncResult.cause())
          }
        }
      }
    }
  }
}
