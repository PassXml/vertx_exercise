/*  大道至简 (C)2021 */
package org.start2do.vertx.web

object WebSetting {
  const val main = "web"
  const val host = "host"
  const val port = "port"
  const val uploadDir = "uploadDir"
  const val resourcesDir = "resourcesDir"
  const val address = "address"
  const val cdnBaseUrl: String = "cdnBaseUrl"

  object Router {
    const val main = "router"
    const val enable = "enable"
  }

  object Auth {
    const val main = "auth"
    const val type = "type"

    object JWT {
      const val main = "jwt"
      const val publicKey = "publicKey"
      const val secretKey = "secretKey"
    }
  }
}

/**
 *  客户端相关
 */
object WehClientInfo {
  const val requestId = "Request-id"
  const val time = "Request-time"
}
