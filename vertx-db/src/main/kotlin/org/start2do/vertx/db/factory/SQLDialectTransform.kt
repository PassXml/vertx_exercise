/*  大道至简 (C)2021 */
package org.start2do.vertx.db.factory

import org.jooq.SQLDialect
import java.util.*

/**
 * @Author Lijie
 * @date 2021/5/16:21:33
 */
object SQLDialectTransform {
  fun get(name: String): SQLDialect {
    return when (name.lowercase(Locale.getDefault())) {
      "postgresql" -> {
        SQLDialect.POSTGRES
      }
      "mysql" -> {
        SQLDialect.MYSQL
      }
      else -> {
        SQLDialect.DEFAULT
      }
    }
  }
}
