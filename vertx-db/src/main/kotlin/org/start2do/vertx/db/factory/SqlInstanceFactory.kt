/*  大道至简 (C)2021 */
package org.start2do.vertx.db.factory

import org.jooq.SQLDialect
import org.start2do.vertx.db.PostgresSqlInstance
import org.start2do.vertx.db.interfaces.SqlInstance

/**
 * @Author Lijie
 * @date 2021/5/16:11:32
 */
object SqlInstanceFactory {
  fun get(enum: SQLDialect): Any =
    when (enum) {
      else -> PostgresSqlInstance()
    }
}
