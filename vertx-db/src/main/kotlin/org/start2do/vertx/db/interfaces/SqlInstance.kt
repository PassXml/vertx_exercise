/*  大道至简 (C)2021 */
package org.start2do.vertx.db.interfaces

import io.vertx.core.Vertx
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnectOptions
import org.jooq.SQLDialect

/**
 * @Author Lijie
 * @date 2021/5/16:10:59
 */
interface SqlInstance {
  fun getType(): SQLDialect


  fun getSqlConnectOptions(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
  ): SqlConnectOptions

  fun getSqlClient(vertx: Vertx, options: SqlConnectOptions, poolOptions: PoolOptions): SqlClient
}
