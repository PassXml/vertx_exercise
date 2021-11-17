/*  大道至简 (C)2021 */
package org.start2do.vertx.db

import io.vertx.core.Vertx
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnectOptions
import org.jooq.SQLDialect
import org.start2do.vertx.db.interfaces.SqlInstance

/**
 * @Author Lijie
 * @date 2021/5/16:11:07
 */
class MysqlInstance : SqlInstance {
  override fun getType(): SQLDialect {
    return SQLDialect.MYSQL
  }

  override fun getSqlConnectOptions(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
  ): SqlConnectOptions = MySQLConnectOptions()
    .setHost(host)
    .setPort(port)
    .setUser(username)
    .setPassword(password)
    .setDatabase(database)

  override fun getSqlClient(vertx: Vertx, options: SqlConnectOptions, poolOptions: PoolOptions): SqlClient =
    MySQLPool.pool(vertx, options as MySQLConnectOptions, poolOptions)
}
