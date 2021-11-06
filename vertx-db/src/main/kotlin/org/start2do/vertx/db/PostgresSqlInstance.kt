/*  大道至简 (C)2021 */
package org.start2do.vertx.db

import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnectOptions
import org.start2do.vertx.db.interfaces.SqlInstance

/**
 * @Author Lijie
 * @date 2021/5/16:11:07
 */
class PostgresSqlInstance : SqlInstance {
  override fun getSqlConnectOptions(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
  ): SqlConnectOptions = PgConnectOptions()
    .setHost(host)
    .setPort(port)
    .setUser(username)
    .setPassword(password)
    .setDatabase(database)

  override fun getSqlClient(vertx: Vertx, options: SqlConnectOptions, poolOptions: PoolOptions): SqlClient =
    PgPool.pool(vertx, options as PgConnectOptions, poolOptions)
}
