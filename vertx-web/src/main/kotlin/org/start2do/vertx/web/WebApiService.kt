package org.start2do.vertx.web

import org.start2do.vertx.web.util.HttpMethod

annotation class WebApiService(
  val address: String = "WEB",
  val mountPath: String
)

annotation class WebApiServiceAction(
  val mountPath: String,
  val method: Array<HttpMethod> = arrayOf(HttpMethod.GET),
  val blocking: Boolean = false
)
