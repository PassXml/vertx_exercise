/*  大道至简 (C)2021 */
package org.start2do.dto

/**
 * @Author Lijie
 * @date 2021/2/10:10:50
 */

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
