import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import org.start2do.tcp.TCPVerticle
import org.start2do.vertx.Deployment
import org.start2do.vertx.VertxRunner
import org.start2do.vertx.pojo.ServiceVerticle
import kotlin.math.log

/**
 * @Author Lijie
 * @date  2021/9/3:11:05
 */

@ServiceVerticle
class Run : TCPVerticle() {
  override fun port(): Int {
    return 81
  }

  override fun getOption(): NetServerOptions {
    return super.getOption().setPort(port())
  }

  override fun closeHandle(sock: NetSocket) {
    logger.info("{},离开了", sock.writeHandlerID())
  }

  override fun handler(buffer: Buffer, sock: NetSocket) {
    logger.info("{},send Msg,{}", sock.writeHandlerID(), buffer.toString())
  }
}

fun main() {
  VertxRunner.run(
    Deployment(
      Run::class.java
    )
  )
}
