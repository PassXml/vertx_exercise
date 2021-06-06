/*  大道至简 (C)2021 */
package org.start2do.vertx.web.ext

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.service.ServiceResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import org.start2do.vertx.MainVerticle
import org.start2do.vertx.dto.BusinessException
import org.start2do.vertx.dto.ResultMessageDto
import org.start2do.vertx.ext.toJsonObject
import org.start2do.vertx.web.WebTop
import org.start2do.vertx.web.utils.outJson
import java.math.BigDecimal
import java.util.*
import kotlin.coroutines.CoroutineContext

inline fun <reified T> RoutingContext.build(): T {
  val clazz = T::class.java
  val contentTypeString = this.request().getHeader("Content-Type")
  val mimeType = if (contentTypeString != null) contentTypeString.split(";")[0].trim() else "text/plain"
  if (mimeType.lowercase(Locale.getDefault()).contains("json")) {
    val constructor = T::class.java.getConstructor(JsonObject::class.java)
      ?: return Json.CODEC.fromValue(this.bodyAsString, T::class.java)
    return constructor.newInstance(this.bodyAsJson)
  } else {
    val resultInstance = clazz.getDeclaredConstructor().newInstance()
    for (declaredField in clazz.declaredFields) {
      declaredField.isAccessible = true
      when (declaredField.type) {
        String::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name])
        }
        java.lang.Integer::class.java,
        Int::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name]?.toInt())
        }
        java.lang.Long::class.java,
        Long::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name]?.toLong())
        }
        java.lang.Double::class.java,
        Double::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name]?.toDouble())
        }
        BigDecimal::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name]?.toBigDecimal())
        }
        java.lang.Float::class.java,
        Float::class.java -> {
          declaredField.set(resultInstance, this.request().formAttributes()[declaredField.name]?.toFloat())
        }
        else -> {
          println(declaredField.type)
        }
      }
    }
    return resultInstance
  }
}

inline fun RoutingContext.getLoginInfo(): Future<User> {
  return this.request().getLoginInfo()
}

inline fun HttpServerRequest.getLoginInfo(): Future<User> {
  var token = this.headers()[HttpHeaders.AUTHORIZATION]
  if (token.isEmpty()) {
    throw BusinessException(401, "没有权限")
  }
  token = token.substring("Bearer ".length)
  return WebTop.TopAuth.authenticate(JsonObject().put("jwt", token))
}

fun CCoroutineExceptionHandler(rc: RoutingContext): CoroutineContext {
  return CoroutineExceptionHandler { _, e ->
    MainVerticle.logger.error("CCoroutineExceptionHandler(RoutingContext)")
    MainVerticle.logger.error(e.message, e)
    (e.cause?.message ?: e.stackTraceToString())
      .outJson(rc, HttpResponseStatus.INTERNAL_SERVER_ERROR)
  }
}

fun ResultMessageDto.Companion.succeed(): ResultMessageDto<*> {
  return build(null)
}

fun ResultMessageDto<*>.futureSucceeded(ctx: io.vertx.core.Handler<io.vertx.core.AsyncResult<ServiceResponse>>) {
  ctx.handle(Future.succeededFuture(ServiceResponse.completedWithJson(this.toJsonObject())))
}

inline fun <reified T> JsonObject.build(block: (T, JsonObject) -> T): T {
  val declaredConstructor = T::class.java.getDeclaredConstructor(JsonObject::class.java)
  val newInstance = declaredConstructor.newInstance(this)
  return block.invoke(newInstance, this)
}

inline fun <reified T> JsonObject.build(): T {
  val declaredConstructor = T::class.java.getDeclaredConstructor(JsonObject::class.java)
  return declaredConstructor.newInstance(this)
}
