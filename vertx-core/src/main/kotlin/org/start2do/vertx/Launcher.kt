/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.logging.Log4j2LogDelegateFactory

/**
 * @Author passxml
 * @date 2020/9/1:12:45
 */
class Launcher : VertxCommandLauncher(), BaseRunner {

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
