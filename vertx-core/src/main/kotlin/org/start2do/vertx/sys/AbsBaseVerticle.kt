/*  大道至简 (C)2021 */
package org.start2do.vertx.sys

import com.google.inject.AbstractModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.start2do.utils.classutil.ClassUtil
import org.start2do.vertx.pojo.GuiceConfiguration
import org.start2do.vertx.*
import org.start2do.vertx.pojo.AutoConfiguration
import org.start2do.vertx.pojo.ServiceVerticle
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.inject.InjectUtil

/**
 * @Author passxml
 * @date 2021/3/13:17:47
 */
abstract class AbsBaseVerticle : AbstractVerticle() {
  protected open val logger = getLogger(Global.SYS)
  open fun authInitBefore() {}
  open fun firstInit() {}
  open fun startAfter() {}
  open fun injectConfig(packages: JsonArray): MutableList<AbstractModule> {
    val result = mutableListOf<AbstractModule>()
    packages.forEach {
      ClassUtil.getPackageClassBySubClass(it.toString(), AbstractModule::class.java).forEach { clazz ->
        val annotation = clazz.getAnnotation(GuiceConfiguration::class.java)
        if (annotation != null) {
          clazz.getDeclaredConstructor(JsonObject::class.java, Vertx::class.java)?.let { constructor ->
            result.add(constructor.newInstance(config(), vertx))
          }
        }
      }
    }
    return result
  }

  override fun init(vertx: Vertx, context: Context) {
    super.init(vertx, context)
    vertx.exceptionHandler {
      logger.error(it.message, it)
    }
  }

  override fun start() {
    firstInit()
    val packages = JsonArray()
    for (s in getAutoConfiguration()) {
      packages.add(s)
    }
    InjectUtil(injectConfig(packages))
    authInitBefore()
    this.javaClass.getAnnotation(ServiceVerticle::class.java)?.let {
      ServiceManager.init(vertx)
      logger.info("扫描包:$packages")
      val address = config().getString(Global.SERVICE_ADDRESS) ?: "service"
      System.setProperty(Global.SERVICE_ADDRESS, address)
      for (i in 0 until packages.size()) {
        ServiceManager.publish(address, packages.getString(i))
      }
    }
    super.start()
    startAfter()
  }

  private fun getAutoConfiguration(): Array<String> {
    var result = arrayOf<String>()
    ClassUtil.getPackageClassBySubClass("org.start2do", AutoConfiguration::class.java).forEach {
      result = result.plus(it.getDeclaredConstructor().newInstance().getScanPackages())
    }
    return result
  }

  final override fun stop() {
    logger.info("正在停止")
    ServiceManager.close()
    super.stop()
  }
}
