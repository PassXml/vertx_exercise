package org.start2do.tcp

import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.start2do.vertx.ext.createFuture
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.sys.AbsBaseVerticle

/**
 * @Author Lijie
 * @date  2021/9/3:10:59
 */
abstract class TCPVerticle : AbsBaseVerticle() {
  open fun port(): Int = 8081

  companion object {
    val Logger = getLogger(TCPVerticle::class.java)
  }

  open fun getOption(): NetServerOptions {
    return NetServerOptions().setPort(port());
  }

  open fun exceptionHandle(throwable: Throwable) {
    Logger.error(throwable.message, throwable)
  }

  open fun closeHandle(sock: NetSocket) {

  }

  open fun connectHandle(sock: NetSocket) {

  }

  open fun drainHandler(sock: NetSocket) {
    sock.resume()
  }

  open fun endHandler(sock: NetSocket) {

  }

  open abstract fun handler(buffer: Buffer, sock: NetSocket)

  override fun start() {
    super.start()
    createFuture {
      val options = getOption()
      val netServer = vertx.createNetServer(options)

      val handler = netServer.connectHandler { sock ->
        sock.closeHandler {
          closeHandle(sock)
        }
        sock.drainHandler {
          drainHandler(sock)
        }
        sock.endHandler {
          endHandler(sock)
        }
        connectHandle(sock)
        sock.handler {
          handler(it, sock)
        }
      }
      handler.exceptionHandler {
        exceptionHandle(it)
      }
      handler.listen().await()
      logger.info("服务已启动,监听端口:{}", options.port)
    }
  }
}

