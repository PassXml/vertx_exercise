/*  大道至简 (C)2021 */
package org.start2do.vertx.db.config

import com.google.inject.AbstractModule
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor
import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnectOptions
import org.flywaydb.core.Flyway
import org.jooq.Configuration
import org.jooq.impl.DefaultConfiguration
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.start2do.vertx.db.*
import org.start2do.vertx.db.factory.SQLDialectTransform
import org.start2do.vertx.db.factory.SqlInstanceFactory
import org.start2do.vertx.db.interfaces.SqlInstance
import org.start2do.vertx.pojo.GuiceConfiguration
import org.start2do.vertx.sys.BaseService
import java.util.*

/**
 * @Author Lijie
 * @date 2021/5/16:10:42
 */
@GuiceConfiguration
class DbConfigModule(private val jsonObject: JsonObject, private val vertx: Vertx) : AbstractModule() {
  companion object {
    private val logger = Log4j2LogDelegateFactory().createDelegate(DbConfigModule::class.java.name)
  }

  override fun configure() {
    val config = DbSetting(jsonObject)
    val host = config.host
    val port = config.port
    val username = config.username
    val password = config.password
    val database = config.database
    val dbType = config.type
    val jdbcUrl = "jdbc:${dbType.lowercase(Locale.getDefault())}://$host:$port/$database"
    logger.info("JDBC URL:{}", jdbcUrl)
    flyway(jdbcUrl, username, password, config.getFlyWaySetting())
    val dbTypeEnum = SQLDialectTransform.get(dbType)
    val configuration = DefaultConfiguration().set(dbTypeEnum)
    bind(Configuration::class.java).toInstance(configuration)
    bind(Vertx::class.java).toInstance(vertx)
    val postgresSqlInstance = SqlInstanceFactory.get(dbTypeEnum) as SqlInstance
    val sqlConnectOptions = postgresSqlInstance.getSqlConnectOptions(host, port, username, password, database)
    val sqlClient = postgresSqlInstance.getSqlClient(vertx, sqlConnectOptions, PoolOptions())
    bind(SqlConnectOptions::class.java).toInstance(sqlConnectOptions)
    bind(SqlClient::class.java).toInstance(sqlClient)
    bind(io.vertx.sqlclient.SqlClient::class.java).toInstance(sqlClient)
    bind(ReactiveClassicGenericQueryExecutor::class.java)
      .toInstance(ReactiveClassicGenericQueryExecutor(configuration, sqlClient))
    val reflections = Reflections(
      ConfigurationBuilder()
        .setScanners(SubTypesScanner())
        .setUrls(ClasspathHelper.forPackage("org.start2do.vertx"))
    )
    val clazzList = arrayOf(AbstractReactiveVertxDAO::class.java, BaseService::class.java)
    for (clazz in clazzList) {
      reflections.getSubTypesOf(clazz).forEach {
        logger.debug("Guice 初始化:{}", it.name)
        requestInjection(it.javaClass)
      }
    }
  }

  private fun flyway(jdbcUrl: String, username: String, password: String, flywayOption: DbSetting.Flyway) {
    if (flywayOption.enable) {
      val flyway = Flyway.configure().dataSource(jdbcUrl, username, password).load()
      when (flywayOption.mode) {
        "baseline" -> {
          flyway.baseline()
        }
        else -> {
          flyway.migrate()
        }
      }
    }
  }
}
