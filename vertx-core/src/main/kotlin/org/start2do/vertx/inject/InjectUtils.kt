/*  大道至简 (C)2020 */
package org.start2do.vertx.inject

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector

/**
 * @Author passxml
 * @date 2020/7/21:09:04
 */
class InjectUtils {
  constructor(array: List<AbstractModule>) {
    instance = Guice.createInjector(array)
  }

  companion object {
    lateinit var instance: Injector
    fun <T> get(clazz: Class<T>): T {
      return instance.getInstance(clazz)
    }
  }
}
