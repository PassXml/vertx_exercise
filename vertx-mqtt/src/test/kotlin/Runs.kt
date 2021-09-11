import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import org.start2do.vertx.Deployment
import org.start2do.vertx.Top
import org.start2do.vertx.VertxRunner

/**
 * @Author Lijie
 * @date  2021/8/26:15:46
 */
fun main() {
  VertxRunner.run(Deployment(MqttVerticle::class.java))
}
