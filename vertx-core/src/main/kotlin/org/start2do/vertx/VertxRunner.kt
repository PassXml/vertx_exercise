package org.start2do.vertx

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystemOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.cluster.NodeListener
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import org.start2do.vertx.ext.createFuture
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.utils.ConfigFileReadUtils
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

/**
 * @Author Lijie
 * @date  2021/9/2:09:24
 */
object VertxRunner {
  private val LOGGER = getLogger(VertxRunner::javaClass.name)
  private suspend fun config(vertx: Vertx): ConfigRetriever {
    val retrieverOptions = ConfigRetrieverOptions()
    try {
      val inputStream = ConfigFileReadUtils.read("config.json")
      retrieverOptions.addStore(
        ConfigStoreOptions().setType("json").setConfig(JsonObject(Buffer.buffer(inputStream.readBytes())))
      )
    } catch (e: FileNotFoundException) {
      LOGGER.error("没有配置文件")
    }
    val retriever: ConfigRetriever = ConfigRetriever.create(
      vertx,
      retrieverOptions
        .addStore(
          ConfigStoreOptions()
            .setType("env")
        ).addStore(
          ConfigStoreOptions().setType("json")
        )
    )

    return retriever
  }

  private fun initGlobal(vertx: Vertx) {
    Top.LongTimeExecutor = vertx.createSharedWorkerExecutor("Long_Time_Block", 5, 10, TimeUnit.MINUTES)
    Top.Breaker = CircuitBreaker.create(
      "Main", vertx,
      CircuitBreakerOptions()
        .setMaxFailures(3)
        .setTimeout(5000)
        .setFallbackOnFailure(true)
    )
    // 更新
    Top.TopClusterManager?.nodeListener(object : NodeListener {
      override fun nodeAdded(nodeID: String?) {
        LOGGER.info("节点:${nodeID}加入")
      }

      override fun nodeLeft(nodeID: String?) {
        LOGGER.info("节点:${nodeID}离开")
      }
    })
    vertx.exceptionHandler {
      LOGGER.error(it.message, it)
    }
    Top.coroutineDispatcher = vertx.dispatcher()
  }

  @JvmStatic
  fun run(vararg deployment: Deployment) {
    runWithOption(null, *deployment)
  }

  @JvmStatic
  fun runWithOption(option: VertxOptions?, vararg deployment: Deployment) {
    val vertx = Vertx.vertx(
      if (option == null) {
        VertxOptions()
          .setBlockedThreadCheckInterval(60)
          .setBlockedThreadCheckIntervalUnit(TimeUnit.SECONDS)
          .setFileSystemOptions(FileSystemOptions().setFileCachingEnabled(false))
      } else {
        option
      }
    )
    initGlobal(vertx)
    createFuture {
      val configRetriever = config(vertx)
      val config = configRetriever.config.await()
      for (deployment in deployment) {
        val deploymentOptions = if (deployment.option == null) {
          val options = DeploymentOptions()
          options.config = if (options.config == null) config else options.config.mergeIn(config)
          options
        } else {
          deployment.option?.config?.mergeIn(config)
          deployment.option
        }
        vertx.deployVerticle(
          deployment.clazz, deploymentOptions
        ).await()
      }
    }
  }
}

class Deployment {

  constructor(clazz: Class<out AbstractVerticle>, option: DeploymentOptions?) {
    this.clazz = clazz
    this.option = option
  }

  constructor(clazz: Class<out AbstractVerticle>) {
    this.clazz = clazz
  }

  var clazz: Class<out AbstractVerticle>
  var option: DeploymentOptions? = null
}
