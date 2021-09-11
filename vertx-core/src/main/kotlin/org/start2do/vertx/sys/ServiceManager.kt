/*  大道至简 (C)2020 */
package org.start2do.vertx.sys

import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.start2do.vertx.Global
import org.start2do.vertx.inject.InjectUtils

object ServiceManager {
  private val logger = Log4j2LogDelegateFactory().createDelegate(Global.SYS)

  private lateinit var serviceBinder: ServiceBinder
  private lateinit var serviceProxyBuilder: ServiceProxyBuilder
  private val serviceSet = mutableSetOf<ServiceDto>()
  private val serviceNameSet = mutableSetOf<String>()
  private val annotationSet: MutableSet<Class<out Annotation>> =
    mutableSetOf(VertxGen::class.java)
  private lateinit var vertx: Vertx

  fun init(vertx: Vertx): ServiceManager {
    this.vertx = vertx
    this.serviceBinder = ServiceBinder(vertx)
    this.serviceProxyBuilder = ServiceProxyBuilder(vertx)

    try {
      val clazz = Class.forName("io.vertx.ext.web.api.service.WebApiServiceGen")
      annotationSet.add(clazz as Class<Annotation>)
    } catch (e: ClassNotFoundException) {
    }

    return this
  }

  fun getServiceProxyBuilder(): ServiceProxyBuilder {
    return this.serviceProxyBuilder
  }

  inline fun <reified T> getServiceName(): String {
    T::class.java.getAnnotation(VertxGen::class.java) ?: return "NoName"
    return T::class.java.name
  }

  fun publish(address: String = (System.getProperty(Global.SERVICE_ADDRESS)), scanPackage: String) {
    val set = Reflections(
      ConfigurationBuilder().setScanners(SubTypesScanner())
        .setUrls(ClasspathHelper.forPackage(scanPackage))
    ).getSubTypesOf(BaseService::class.java)
    for (clazz in set) {
      if (serviceNameSet.contains(clazz.name)) {
        continue
      }
      for (face in clazz.interfaces) {
        for (clazz1 in annotationSet) {
          face.getAnnotation(clazz1) ?: continue
          val serviceName = face.simpleName
          val registerInfo = InjectUtils.get(clazz).register(serviceBinder.setAddress("$address.$serviceName"))
          logger.info("推送服务{},{}", serviceName, registerInfo.isRegistered)
          serviceSet.add(
            ServiceDto(
              clazz.name,
              null,
              registerInfo
            )
          )
          serviceNameSet.add(clazz.name)
          break
        }
      }
    }
  }

  fun close() {
    for (s in serviceSet) {
      serviceBinder.unregister(s.registerInfo)
    }
  }
}

data class ServiceDto(
  val name: String,
  val id: String? = null,
  val registerInfo: MessageConsumer<JsonObject>? = null
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ServiceDto
    if (name != other.name) return false
    return true
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }
}
