/*  大道至简 (C)2020 */
package org.start2do.api

import io.vertx.core.json.JsonObject

object APIUtils {
  @Synchronized
  fun getName(clazz: Class<*>): JsonObject {
    return JsonObject().put("name", clazz.simpleName).put("address", clazz.simpleName)
  }
}
