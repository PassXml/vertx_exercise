/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.cancel
import org.start2do.vertx.config.VertxOptionBuilder
import org.start2do.vertx.utils.ConfigFileReadUtils
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

/**
 * @Author passxml
 * @date 2020/9/1:12:45
 */
class Launcher : VertxCommandLauncher, VertxLifecycleHooks {
  constructor()

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
    vertx.dispatcher().cancel(CancellationException("退出"))
    vertx.close()
  }

  override fun beforeStartingVertx(options: VertxOptions) {
    VertxOptionBuilder.build(
      options,
      JsonObject(
        Buffer.buffer(
          ConfigFileReadUtils.read("config.json").readAllBytes()
        )
      )
    )
    options.setBlockedThreadCheckInterval(30)
      .blockedThreadCheckIntervalUnit = TimeUnit.SECONDS
  }

  companion object {
    @JvmStatic
    fun executeCommand(cmd: String?, vararg args: String?) {
      Launcher().execute(cmd, *args)
    }

    @JvmStatic
    fun main(args: Array<String>) {
      Launcher().dispatch(args)
    }

    private val logger = Log4j2LogDelegateFactory().createDelegate(Launcher::class.java.name)
  }
}
