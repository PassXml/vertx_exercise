/*  大道至简 (C)2021 */
package org.start2do.vertx.web.dto

import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonObject
import org.start2do.vertx.web.util.MIMEType

/**
 * @Author Lijie
 * @date 2021/2/10:10:50
 */
data class HttpRequest(
  val host: String,
  val cookies: Set<Cookie>? = null,
  val headers: MultiMap? = null,
  val params: MultiMap? = null,
  val body: Buffer? = null,
  val form: MultiMap? = null
) {
  fun bodyAsJson(): JsonObject {
    return body?.toJsonObject() ?: JsonObject()
  }
}

data class HttpRespose(
  val status: Int,
  val headers: MultiMap? = null,
  val body: Buffer,
  val ContentType: MIMEType = MIMEType.JSON
)

enum class ContentTypeEnum(val value: String) {
  JSON("application/json; charset=utf-8"),
  HTML("text/html;charset=utf-8"),
  TEXT("text/plain;charset=utf-8"),
  ImageJpeg("image/jpeg");
}

data class KeyValue(
  val key: String,
  val value: String
)

data class KeyValues(
  val key: String,
  val values: List<String>
)

data class RemoteInfo(
  val host: String,
  val port: Int
)
