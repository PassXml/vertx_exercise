/*  大道至简 (C)2020 */
package org.start2do.vertx.sys


import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ServiceBinder

import org.start2do.api.AbsService


/**
 * @Author passxml
 * @date 2020/7/15:14:10
 */
abstract class BaseService : AbsService {

  abstract fun register(serviceBinder: ServiceBinder): MessageConsumer<JsonObject>

}
