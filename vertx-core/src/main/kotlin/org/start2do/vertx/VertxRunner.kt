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
import kotlinx.coroutines.runBlocking
import org.start2do.vertx.config.NetWorkSetting
import org.start2do.vertx.ext.createFuture
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.utils.ConfigFileReadUtil
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

/**
 * @Author Lijie
 * @date  2021/9/2:09:24
 */
object VertxRunner {
  private val LOGGER = getLogger(VertxRunner::javaClass.name)
  lateinit var vertx: Vertx


  private fun config(vertx: Vertx): ConfigRetriever {
    val retrieverOptions = ConfigRetrieverOptions()
    try {
      val inputStream = ConfigFileReadUtil.read("config.json")
      val jsonObject = JsonObject(
        Buffer.buffer(inputStream.readAllBytes())
      )
      retrieverOptions.addStore(
        ConfigStoreOptions().setType("json").setConfig(
          jsonObject
        )
      )
    } catch (e: FileNotFoundException) {
      LOGGER.error("没有配置文件")
    }
    return ConfigRetriever.create(
      vertx, retrieverOptions.addStore(
        ConfigStoreOptions().setType("env")
      ).addStore(
        ConfigStoreOptions().setType("json")
      ).setScanPeriod(900)
    )
  }

  private suspend fun setNetworkConfig(verticleClazz: Class<out AbstractVerticle>, jsonObject: JsonObject): JsonObject {
    NetWorkSetting.from(jsonObject)?.let { networkSetting ->
      val clazz = Class.forName(networkSetting.className)
      if (GetNetworkSetting::class.java.isAssignableFrom(clazz)) {
        val instance = clazz.getDeclaredConstructor().newInstance() as GetNetworkSetting
        if (networkSetting.flag.isNullOrBlank()) {
          networkSetting.flag = verticleClazz.simpleName
        }
        return try {
          instance.get(vertx, networkSetting).await().mergeIn(jsonObject)
        } catch (e: Throwable) {
          JsonObject()
        }
      }
    }
    return jsonObject
  }

  private fun initGlobal() {
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
    vertx = Vertx.vertx(
      option ?: VertxOptions().setBlockedThreadCheckInterval(60).setBlockedThreadCheckIntervalUnit(TimeUnit.SECONDS)
        .setFileSystemOptions(FileSystemOptions().setFileCachingEnabled(false))
    )
    runBlocking {
      val config = config(vertx).config.await()
      initGlobal()
      for (deployment in deployment) {
        val deploymentOptions = if (deployment.option == null) {
          val options = DeploymentOptions()
          val jsonObject = if (options.config == null) config else options.config.mergeIn(config)
          options.config = setNetworkConfig(deployment.clazz, jsonObject)
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
