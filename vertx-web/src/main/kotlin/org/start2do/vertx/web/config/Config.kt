/*  大道至简 (C)2021 */
package org.start2do.vertx.web.config

data class ApplicationConfig(
  val uploadDir: String?,
  val resourcesDir: String?,
  val port: Int = 8888,
  val cdnBaseUrl: String
)
