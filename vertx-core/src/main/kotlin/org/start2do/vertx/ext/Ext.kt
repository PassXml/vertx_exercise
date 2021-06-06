/*  大道至简 (C)2020 */
package org.start2do.vertx.ext

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.*
import org.start2do.api.AbsService
import org.start2do.vertx.MainVerticle
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

/**
 * @Author HelloBox@outlook.com
 * @date 2020/8/20:19:50
 */

fun Any.toJsonObject(): JsonObject {
  return JsonObject.mapFrom(this)
}

fun String.toJsonObject(): JsonObject {
  return JsonObject(this)
}

fun List<Any>.toJsonArray(): JsonArray {
  return JsonArray(this)
}

inline fun <reified T> T.clone(): T {
  val to = T::class.java.getDeclaredConstructor().newInstance()
  T::class.java.declaredFields.forEach { field ->
    val isLocked = field.trySetAccessible()
    field.isAccessible = true
    if (field.get(this) != null) {
      field.set(to, field.get(this))
    }
    field.isAccessible = isLocked
  }
  return to
}

fun <T> CCoroutineExceptionHandler(ctx: Message<T>): CoroutineExceptionHandler {
  return CoroutineExceptionHandler { _, e ->
    MainVerticle.logger.error("CCoroutineExceptionHandler(Message)")
    MainVerticle.logger.error(e.message, e)
    when (e) {
      is ReplyException -> {
        ctx.fail(e.failureCode(), e.message)
      }
      else -> {
        ctx.fail(-1, e.message)
      }
    }
  }
}

fun <T> CCoroutineExceptionHandler(ctx: Handler<AsyncResult<T>>): CoroutineExceptionHandler {
  return CoroutineExceptionHandler { _, e ->
    MainVerticle.logger.error("CCoroutineExceptionHandler(AsyncResult)")
    MainVerticle.logger.error(e.message, e)
    when (e) {
      else -> {
        ctx.handle(Future.failedFuture(e))
      }
    }
  }
}

fun CCoroutineExceptionHandler(): CoroutineContext {
  return CoroutineExceptionHandler { _, e ->
    MainVerticle.logger.error("CCoroutineExceptionHandler(null)")
    MainVerticle.logger.error(e.message, e)
  }
}

inline fun <T> createResultHandle(handle: Handler<AsyncResult<T>>, crossinline block: suspend (CoroutineScope) -> T) {
  CoroutineScope(SupervisorJob()).launch(CCoroutineExceptionHandler(handle)) {
    handle.handle(Future.succeededFuture(block(this)))
  }
}

// fun AbsService.release() {
//  ServiceDiscovery.releaseServiceObject(ServiceManager.get(), this)
// }
suspend inline fun <T, R : AbsService> R.handler(crossinline block: suspend (R) -> T): T {
  return block(this)
}

inline fun <T, R : AbsService> R.blockingHandler(crossinline block: suspend (R) -> T): T {
  val self = this
  return runBlocking<T> {
    block(self)
  }
}

inline fun <T> createFuture(
  crossinline block: suspend (CoroutineScope) -> T
): Future<T> = Future.future { promise ->
  CoroutineScope(SupervisorJob()).launch(CCoroutineExceptionHandler()) {
    try {
      promise.complete(block(this))
    } catch (e: Exception) {
      promise.fail(e)
    }
  }
}

fun <T> Deferred<T>.asCompletableFuture(): CompletableFuture<T> {
  val future = CompletableFuture<T>()
  setupCancellation(future)
  invokeOnCompletion {
    try {
      future.complete(getCompleted())
    } catch (t: Throwable) {
      future.completeExceptionally(t)
    }
  }
  return future
}

private fun Job.setupCancellation(future: CompletableFuture<*>) {
  future.whenComplete { _, exception ->
    cancel(
      exception?.let {
        it as? CancellationException ?: CancellationException(
          "CompletableFuture was completed exceptionally",
          it
        )
      }
    )
  }
}
