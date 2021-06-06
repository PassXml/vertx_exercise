/*  大道至简 (C)2020 */
package org.start2do.vertx.utils

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.json.jackson.DatabindCodec

object JacksonFix {
  var isFixed: Boolean = false
  fun fixVertxJson() {
    if (!isFixed) {
      DatabindCodec.mapper().registerKotlinModule().findAndRegisterModules()
      isFixed = true
    }
  }
}
