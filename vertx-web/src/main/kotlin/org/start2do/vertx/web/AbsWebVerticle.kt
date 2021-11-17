/*  大道至简 (C)2020 */
package org.start2do.vertx.web

import com.google.inject.AbstractModule
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.ext.web.openapi.RouterBuilderOptions
import io.vertx.ext.web.validation.BodyProcessorException
import io.vertx.ext.web.validation.ParameterProcessorException
import io.vertx.kotlin.coroutines.await
import io.vertx.serviceproxy.ServiceException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.start2do.vertx.Top
import org.start2do.vertx.ext.CCoroutineExceptionHandler
import org.start2do.vertx.inject.InjectUtil
import org.start2do.vertx.pojo.Configuration
import org.start2do.vertx.sys.AbsBaseVerticle
import org.start2do.vertx.web.util.ControllerManager
import org.start2do.vertx.web.util.outJson
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @Author passxml
 * @date 2020/9/6:11:47
 */
abstract class AbsWebVerticle : AbsBaseVerticle() {
  open fun getAuth(): AuthenticationProvider? {
    val webSetting = config().getJsonObject(WebSetting.main) ?: JsonObject()
    val authConfig = webSetting.getJsonObject(WebSetting.Auth.main) ?: return null
    when (authConfig.getString(WebSetting.Auth.type)) {
      WebSetting.Auth.JWT.main -> {
        logger.info("[SYS]使用JWT权限校验器")
        val jwtConfig = authConfig.getJsonObject(WebSetting.Auth.JWT.main)
        val jwtAuthOptions = JWTAuthOptions().addPubSecKey(
          PubSecKeyOptions().setAlgorithm("RS256").setBuffer(
            jwtConfig.getString(WebSetting.Auth.JWT.publicKey)
          )
        ).addPubSecKey(
          PubSecKeyOptions().setAlgorithm("RS256").setBuffer(
            jwtConfig.getString(WebSetting.Auth.JWT.secretKey)
          )
        )
        return JWTAuth.create(vertx, jwtAuthOptions)
      }
      else -> {
        logger.info("[SYS]没有权限验证器")
        return null
      }
    }
  }

  lateinit var router: Router

  open fun buildRouter() {
    val countDownLatch = CountDownLatch(1)
    CoroutineScope(Top.coroutineDispatcher).launch(CCoroutineExceptionHandler()) {
      val webSetting = config().getJsonObject(WebSetting.main) ?: JsonObject()
      val config = webSetting.getJsonObject(WebSetting.Router.main) ?: JsonObject()
      if (config.getBoolean(WebSetting.Router.enable, false)) {
        logger.info("[SYS]根据OpenApi3.0描述文件构建路由")
        val build = RouterBuilder.create(vertx, config.getString("file", "./route.yml")).await()
        build.options = RouterBuilderOptions()
          .setContractEndpoint(config.getString("contractEndpoint", ""))
        router = build.createRouter()
      } else {
        logger.info("[SYS]Router配置文件为空,创建空路由对象{}", config)
        router = Router.router(vertx)
      }
      countDownLatch.countDown()
    }
    countDownLatch.await(30, TimeUnit.SECONDS)
  }

  open fun errorHandler() {
    logger.info("[SYS]初始化错误处理器")
    val function = { rc: RoutingContext ->
      val failure = rc.failure()
      when (failure) {
        is ParameterProcessorException -> {

          outJson(
            rc, HttpResponseStatus.BAD_REQUEST, code = -1,
            msg = when (failure.errorType) {
              ParameterProcessorException.ParameterProcessorErrorType.MISSING_PARAMETER_WHEN_REQUIRED_ERROR -> {
                "缺少必要参数[${failure.parameterName}]"
              }
              ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR -> {
                "转化[${failure.parameterName}]失败"
              }
              ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR -> {
                "[${failure.parameterName}]格式不正确"
              }
            }
          )
        }
        is BodyProcessorException -> {
          logger.debug(
            "{}({})\nQuery:{}\rBody:{}",
            rc.request().path(),
            rc.request().remoteAddress(),
            rc.queryParams(),
            rc.bodyAsString
          )
          logger.error("BodyProcessorException:{}", failure.message)
          logger.error("${rc.request().path()}", failure)
          outJson(
            rc, HttpResponseStatus.BAD_REQUEST, code = -1,
            msg = when (failure.errorType) {
              BodyProcessorException.BodyProcessorErrorType.MISSING_MATCHING_BODY_PROCESSOR -> {
                "没有合适的处理器"
              }
              BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR -> {
                "格式化失败"
              }
              BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR -> {
                "校验失败"
              }
            }
          )
        }
        is ServiceException -> {
          outJson(
            rc, HttpResponseStatus.INTERNAL_SERVER_ERROR, code = failure.failureCode(),
            msg = failure.message
          )
        }
      }
    }
    this.router.errorHandler(
      400, function
    )
    this.router.errorHandler(500, function)
  }

  private fun initSysConfig(httpSetting: JsonObject) {
    System.setProperty(
      "${WebSetting.main}.${WebSetting.address}",
      httpSetting.getString(WebSetting.address) ?: WebSetting.address
    )
  }

  open override fun injectConfig(packages: JsonArray): MutableList<AbstractModule> {
    val config = super.injectConfig(packages)
    config.add(
      object : AbstractModule() {
        override fun configure() {
          bind(Vertx::class.java).toInstance(getVertx())
          bind(Router::class.java).toInstance(router)
          super.configure()
        }
      })
    return config
  }

  final override fun start() {
    val httpSetting = config().getJsonObject(WebSetting.main, JsonObject())
    logger.info("[SYS]第一次初始化")
    firstInit()
    val packages = JsonArray()
    this.javaClass.getAnnotation(Configuration::class.java)?.let {
      packages.add(this.javaClass.`package`.name)
      for (pkg in it.scanPackages) {
        packages.add(pkg)
      }
    }
    logger.info("[SYS]初始化路由")
    buildRouter()
    logger.info("[SYS]初始化Guice")
    InjectUtil(injectConfig(packages))
    authInitBefore()
    getAuth()?.let {
      WebTop.TopAuth = it
    }
    for (i in 0 until packages.size()) {
      logger.info("[SYS]初始化Controller[{}]", packages.getString(i))
      ControllerManager.start(this.router, vertx, packages.getString(i))
    }
    errorHandler()
    val port = httpSetting.getInteger(WebSetting.port, 8080)
    initSysConfig(httpSetting)
    vertx.createHttpServer().requestHandler(router).listen(port).onSuccess {
      logger.info("[SYS]HTTP 服务监听于:{}", port)
      super.start()
      startAfter()
    }.onFailure {
      logger.error("[SYS]服务启动失败,${it.message}", it)
    }
  }
}
