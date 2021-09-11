/*  大道至简 (C)2020 */
package org.start2do.vertx.web.utils

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.WorkerExecutor
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.service.RouteToEBServiceHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.kotlin.coroutines.await
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.start2do.vertx.Global
import org.start2do.vertx.Top
import org.start2do.vertx.pojo.ResultMessageDto
import org.start2do.vertx.ext.createAsyncTask
import org.start2do.vertx.ext.getLogger
import org.start2do.vertx.inject.InjectUtils
import org.start2do.vertx.web.WehClientInfo
import java.io.ByteArrayOutputStream
import java.lang.reflect.Method
import java.net.URLEncoder
import java.util.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

/**
 * @Author passxml
 * @date 2020/9/3:14:14
 */
object ControllerManager {
  private val logger = getLogger("WebLog")
  private val initSet = mutableSetOf<String>()
  private val urlSet = mutableSetOf<String>()
  fun start(router: Router, vertx: Vertx, packages: String) {
    val set = Reflections(
      ConfigurationBuilder()
        .setScanners(SubTypesScanner())
        .setUrls(ClasspathHelper.forPackage(packages))
    ).getSubTypesOf(BaseController::class.java)
    set.forEach { clazz ->
      if (initSet.contains(clazz.name)) {
        return@forEach
      }
      initSet.add(clazz.name)
      val instance = InjectUtils.get(clazz) ?: clazz.getDeclaredConstructor(Router::class.java, Vertx::class.java)
        .newInstance(router, vertx)
      instance.build()
      instance.longTimeExecutor = Top.LongTimeExecutor
      var baseMountPath = clazz.getAnnotation(Controller::class.java)?.mountPath ?: ""
      for (method in clazz.methods) {
        val controller = method.getAnnotation(Controller::class.java) ?: continue
        var route = createRouteMethod(controller, router)
        val formatUrl = formatUrl(baseMountPath, controller.mountPath)
        if (urlSet.contains(formatUrl)) {
          continue
        }
        urlSet.add(formatUrl)
        logger.info("处理URL为:{}", formatUrl)
        if (controller.useRoute) {
          logger.info("useRoute")
          method.invoke(instance, route.pathRegex(formatUrl).handler {
            setCountInfo(it)
          })
        } else if (controller.useWebSocket) {
          logger.info("useWebSocket")
          route.pathRegex(formatUrl).handler { rc ->
            rc.request().pause()
            createAsyncTask {
              method.callMethod(instance, rc.request().toWebSocket().await())
            }
          }
        } else {
          val rootHandler = route.pathRegex(formatUrl).handler(BodyHandler.create()).handler {
            setCountInfo(it)
          }
          createAsyncTask {
            when (true) {
              Handler::class.java.isAssignableFrom(method.returnType), WebServiceHandles::class.java == method.returnType -> {
                val result = method.callMethod(instance, rootHandler)
                if (method.returnType == WebServiceHandles::class.java) {
                  val (validationHandler, serviceHandler) = result as WebServiceHandles
                  eventBusService(
                    clazz, method, rootHandler, controller.blocking, validationHandler, serviceHandler
                  )
                } else {
                  eventBusService(
                    clazz, method, rootHandler, controller.blocking, result as Handler<RoutingContext>
                  )
                }
              }
              else -> {
                logger.info("{},{},使用Router Handle", clazz.simpleName, method.name)
                val function = Handler<RoutingContext> { rc ->
                  createAsyncTask {
                    var params = arrayOf<Any>()
                    for (parameter in method.parameters) {
                      when (true) {
                        RoutingContext::class.java.isAssignableFrom(parameter.type) -> {
                          params = params.plusElement(rc)
                        }
                        HttpServerRequest::class.java.isAssignableFrom(parameter.type) -> {
                          params = params.plusElement(rc.request())
                        }
                        HttpServerResponse::class.java.isAssignableFrom(parameter.type) -> {
                          params = params.plusElement(rc.response())
                        }
                      }
                    }
                    val result = method.callMethod(instance, *params)
                    if (!rc.response().closed()) {
                      (result ?: "").outJson(rc)
                    }
                  }
                }
                if (controller.blocking) {
                  rootHandler.blockingHandler(function)
                } else {
                  rootHandler.handler(function)
                }
              }
            }
          }
        }
      }
    }
  }

  private fun createRouteMethod(controller: Controller, router: Router): Route {
    return when (controller.method) {
      HttpMethod.HEAD -> {
        router.head()
      }
      HttpMethod.PUT -> {
        router.put()
      }
      HttpMethod.POST -> {
        router.post()
      }
      HttpMethod.PATCH -> {
        router.patch()
      }
      HttpMethod.TRACE -> {
        router.trace()
      }
      HttpMethod.DELETE -> {
        router.delete()
      }
      HttpMethod.OPTIONS -> {
        router.options()
      }
      else -> {
        router.get()
      }
    }
  }


  private inline fun setCountInfo(rc: RoutingContext) {
    rc.put(WehClientInfo.time, System.currentTimeMillis())
    val requestId = UUID.randomUUID().toString().replace("-", "")
    rc.put(WehClientInfo.requestId, requestId)
    rc.response().putHeader(WehClientInfo.requestId, requestId)
    rc.response().endHandler {
      logger.info(
        "Request:{},请求:{},耗时:{}ms",
        rc.get<String>(WehClientInfo.requestId),
        rc.request().uri(),
        System.currentTimeMillis() - rc.get<Long>(WehClientInfo.time)
      )
    }
    rc.next()
  }


  private fun formatUrl(vararg urls: String): String {
    val stringJoiner = StringJoiner("")
    for (s in urls) {
      if (s.isBlank()) {
        continue
      }
      var url = if (s.startsWith("/")) {
        s.substring(1)
      } else {
        s
      }
      url = if (url.endsWith("/")) {
        url.substring(url.length - 1)
      } else {
        url
      }
      stringJoiner.add("/").add(url)
    }
    return stringJoiner.toString()
  }

  private fun eventBusService(
    clazz: Class<*>,
    method: Method,
    rootHandler: Route,
    blocking: Boolean,
    vararg handles: Handler<RoutingContext>
  ) {
    logger.info("{},{},使用EventBus Router Handle", clazz.simpleName, method.name)
    for (handle in handles) {
      if (blocking) {
        rootHandler.blockingHandler(handle)
      } else {
        rootHandler.handler(handle)
      }
    }
  }
}

suspend inline fun Method.callMethod(instance: BaseController, vararg params: Any?): Any? {
  return if (this.parameters.isEmpty()) {
    if (this.kotlinFunction != null && this.kotlinFunction!!.isSuspend) {
      this.kotlinFunction!!.callSuspend(instance)
    } else {
      this.invoke(instance)
    }
  } else {
    if (this.kotlinFunction != null && this.kotlinFunction!!.isSuspend) {
      this.kotlinFunction!!.callSuspend(instance, *params)
    } else {
      this.invoke(instance, *params)
    }
  }


}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class Controller(
  val mountPath: String,
  val method: HttpMethod = HttpMethod.GET,
  val blocking: Boolean = false,
  val useRoute: Boolean = false,
  val useWebSocket: Boolean = false,
)

enum class HttpMethod {
  GET, HEAD, POST, PUT, PATCH, DELETE, TRACE, OPTIONS
}

abstract class BaseController(protected val router: Router, protected val vertx: Vertx) {
  lateinit var longTimeExecutor: WorkerExecutor

  open fun build(): BaseController {
    return this
  }
}

fun Any.outJson(rc: RoutingContext, error: HttpResponseStatus? = null, msg: String? = null, code: Int = 0) {
  val response = rc.response().putHeader("Content-type", "application/json; charset=UTF-8")
  if (msg == null) {
    var result: Any? = null
    if (!BaseController::class.java.isAssignableFrom(this.javaClass) && !this.javaClass.name.startsWith("kotlinx.coroutines")) {
      result = this
    }
    response
      .setStatusCode(error?.code() ?: HttpResponseStatus.OK.code())
      .end(DatabindCodec.mapper().writeValueAsString(ResultMessageDto.build(code, result)))
  } else {
    response
      .setStatusCode(error?.code() ?: HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
      .end(
        DatabindCodec.mapper().writeValueAsString(ResultMessageDto.build(code, msg))
      )
  }
}

fun ByteArrayOutputStream.end(rc: RoutingContext, name: String) {
  this.use {
    rc.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
      .putHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename* = UTF-8''" + URLEncoder.encode(name, "UTF-8")
      )
      .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
      .end(Buffer.buffer().appendBytes(this.toByteArray()))
  }
}

data class WebServiceHandles(
  val validationHandler: ValidationHandler,
  val serviceHandler: RouteToEBServiceHandler
)
