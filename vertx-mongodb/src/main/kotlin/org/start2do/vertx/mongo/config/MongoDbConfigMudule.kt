package org.start2do.vertx.mongo.config

import com.google.inject.AbstractModule
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.start2do.vertx.ext.toJsonObject
import org.start2do.vertx.mongo.MongoDbSetting
import org.start2do.vertx.pojo.GuiceConfiguration

/**
 * @Author Lijie
 * @date  2021/10/26:17:21
 */
@GuiceConfiguration
class MongoDbConfigMudule(private val jsonObject: JsonObject, private val vertx: Vertx) : AbstractModule() {
  override fun configure() {
    val setting = MongoDbSetting(jsonObject)
    bind(MongoClient::class.java).toInstance(MongoClient.createShared(vertx, setting.toJsonObject()))
  }
}
