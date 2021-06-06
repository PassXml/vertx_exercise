/*  大道至简 (C)2020 */
package org.start2do.vertx.sys

import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ServiceBinder

/**
 * @Author passxml
 * @date 2020/7/15:14:10
 */
abstract class BaseService {

  abstract fun register(serviceBinder: ServiceBinder): MessageConsumer<JsonObject>
}
