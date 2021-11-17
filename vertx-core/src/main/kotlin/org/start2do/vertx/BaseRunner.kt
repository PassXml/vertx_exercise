package org.start2do.vertx

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.cancel
import org.start2do.vertx.config.VertxOptionBuilder
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.utils.ConfigFileReadUtil
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

/**
 * @Author Lijie
 * @date  2021/9/2:09:19
 */
interface BaseRunner : VertxLifecycleHooks {
  companion object {
    private val logger = getLogger(BaseRunner::javaClass.name)
  }

  override fun afterConfigParsed(config: JsonObject?) {}
  override fun afterStartingVertx(vertx: Vertx?) {}

  override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions?) {}

  override fun beforeStoppingVertx(vertx: Vertx?) {}

  override fun afterStoppingVertx() {}
  override fun handleDeployFailed(
    vertx: Vertx,
    mainVerticle: String?,
    deploymentOptions: DeploymentOptions?,
    cause: Throwable?
  ) {
    logger.error("发生错误", cause)
    vertx.dispatcher().cancel(CancellationException("退出"))
    vertx.close()
  }

  override fun beforeStartingVertx(options: VertxOptions) {
    VertxOptionBuilder.build(
      options,
      JsonObject(
        Buffer.buffer(
          ConfigFileReadUtil.read("config.json").readAllBytes()
        )
      )
    )
    options.setBlockedThreadCheckInterval(30)
      .blockedThreadCheckIntervalUnit = TimeUnit.SECONDS
  }

}
