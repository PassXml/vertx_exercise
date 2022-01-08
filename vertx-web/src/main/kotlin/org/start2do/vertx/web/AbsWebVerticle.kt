/*  大道至简 (C)2020 */
package org.start2do.vertx.web

import com.google.inject.AbstractModule
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.ext.web.openapi.RouterBuilderOptions
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.start2do.vertx.ext.ExceptionHandler
import org.start2do.vertx.inject.InjectUtil
import org.start2do.vertx.sys.AbsBaseVerticle
import org.start2do.vertx.web.util.ControllerManager

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
    runBlocking(ExceptionHandler()) {
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
    }
  }

  open fun errorHandler() {
    logger.info("[SYS]初始化错误处理器")
    this.router.errorHandler(400, errorHandler)
    this.router.errorHandler(500, errorHandler)
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
    logger.info("[SYS]初始化路由")
    buildRouter()
    logger.info("[SYS]初始化Guice")
    val packages = getPackages()
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
    val port = httpSetting.getInteger(WebSetting.port, 7070)
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
