package org.start2do.vertx.web

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.BodyProcessorException
import io.vertx.ext.web.validation.ParameterProcessorException
import io.vertx.serviceproxy.ServiceException
import org.start2do.vertx.Top.logger
import org.start2do.vertx.web.util.outJson

/**
 * @Author Lijie
 * @date  2021/12/14:20:08
 */
val errorHandler = Handler<RoutingContext> { rc: RoutingContext ->
  when (val failure = rc.failure()) {
    is ParameterProcessorException -> {
      when (failure.errorType) {
        ParameterProcessorException.ParameterProcessorErrorType.MISSING_PARAMETER_WHEN_REQUIRED_ERROR -> {
          "缺少必要参数[${failure.parameterName}]"
        }
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR -> {
          "转化[${failure.parameterName}]失败"
        }
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR -> {
          "[${failure.parameterName}]格式不正确"
        }
      }.outJson(
        rc, HttpResponseStatus.BAD_REQUEST, code = -1,
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
      when (failure.errorType) {
        BodyProcessorException.BodyProcessorErrorType.MISSING_MATCHING_BODY_PROCESSOR -> {
          "没有合适的处理器"
        }
        BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR -> {
          "格式化失败"
        }
        BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR -> {
          "校验失败"
        }
      }.outJson(
        rc, HttpResponseStatus.BAD_REQUEST, code = -1
      )
    }
    is ServiceException -> {
      failure.message?.outJson(
        rc, HttpResponseStatus.INTERNAL_SERVER_ERROR, code = failure.failureCode(),
      )
    }
    else -> {
      logger.error(failure.message, failure)
    }
  }
}
