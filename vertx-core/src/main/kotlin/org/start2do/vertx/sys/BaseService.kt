/*  大道至简 (C)2020 */
package org.start2do.vertx.sys


import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ServiceBinder
import javassist.runtime.Desc.getClazz

import org.start2do.api.AbsService
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * @Author passxml
 * @date 2020/7/15:14:10
 */
abstract class BaseService<T : AbsService> : AbsService {
  val type: Type by lazy {
    val superClass = this.javaClass.genericSuperclass
    (superClass as ParameterizedType).actualTypeArguments[0]
  }


  open fun getAddress(): String {
    return type.typeName
  }

  open fun register(serviceBinder: ServiceBinder): MessageConsumer<JsonObject> {
    println(getAddress())
    println(type as Class<T>)
    return serviceBinder.setAddress(getAddress()).register(type as Class<T>, this as T)
  }

}
