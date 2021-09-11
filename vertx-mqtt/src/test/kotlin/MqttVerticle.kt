import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mqtt.MqttServer
import org.start2do.vertx.ext.createFuture
import org.start2do.vertx.sys.AbsBaseVerticle


/**
 * @Author Lijie
 * @date  2021/8/26:15:35
 */
open class MqttVerticle : AbsBaseVerticle() {
  override fun start() {
    val mqttServer = MqttServer.create(vertx)
    createFuture {
      mqttServer.endpointHandler { endpoint ->
        System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());
        if (endpoint.auth() != null) {
          System.out.println(
            "[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]"
          );
        }
        if (endpoint.will() != null) {
          System.out.println(
            "[will topic = " + endpoint.will().willTopic + " msg = " + endpoint.will()
              .willMessageBytes?.toString() +
              " QoS = " + endpoint.will().willQos + " isRetain = " + endpoint.will().isWillRetain + "]"
          );
        }

        System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");
        // ACCEPT CONNECTION FROM THE REMOTE CLIENT
        endpoint.accept(false);
        endpoint.publishHandler { pushMessage ->
          println(pushMessage.topicName())
          println(pushMessage.payload().toString("utf8"))
        }
        endpoint.disconnectHandler { v -> println("客户断开连接") }
        endpoint.subscribeHandler { subscribe ->
          val grantedQosLevels: MutableList<MqttQoS> = ArrayList()
          for (s in subscribe.topicSubscriptions()) {
            System.out.println("Subscription for " + s.topicName().toString() + " with QoS " + s.qualityOfService())
            grantedQosLevels.add(s.qualityOfService())
          }
//          endpoint.publishAcknowledge()
          // ack the subscriptions request
          endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels)
        }
        endpoint.unsubscribeHandler { subscribe ->
          endpoint.unsubscribeAcknowledge(subscribe.messageId())
        }
        endpoint.publish(
          "测试", Buffer.buffer("测试1"), MqttQoS.AT_LEAST_ONCE,
          false, false
        )
      }

      mqttServer.listen(8080).await()
      println("部署成功")
    }
  }
}
