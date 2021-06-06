/*  大道至简 (C)2020 */
package org.start2do.vertx.utils

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @Author HelloBox@outlook.com
 * @date 2020/7/7:19:27
 */
class Symbol(private val symbolName: String) : InvocationHandler {
  /**
   * A marker interface to indicate that an object is a symbol.
   */
  private interface ISymbol

  /**
   * Checks whether a given object is a symbol.
   *
   * @param o
   * object to test
   * @return true if it is a symbol.
   */
  fun isSymbol(o: Any?): Boolean {
    return o is ISymbol
  }

  companion object {
    fun <T> newSymbol(clazz: Class<T>): T {
      return newSymbol(clazz, clazz.name)
    }

    fun <T> newSymbol(
      clazz: Class<T>,
      name: String
    ): T {
      return try {
        Proxy.newProxyInstance(
          Symbol::class.java.classLoader,
          arrayOf(
            clazz,
            ISymbol::class.java
          ),
          Symbol(name)
        ) as T
      } catch (e: IllegalArgumentException) {
        throw ExceptionInInitializerError(e)
      }
    }
  }

  @Throws(Throwable::class)
  override fun invoke(
    proxy: Any,
    method: Method,
    args: Array<Any>
  ): Any? {
    if ("equals" == method.getName()) {
      return proxy === args[0]
    } else if ("hashCode" == method.getName()) {
      return symbolName.hashCode()
    } else if ("toString" == method.getName()) {
      return symbolName
    }
    throw UnsupportedOperationException()
  }
}
