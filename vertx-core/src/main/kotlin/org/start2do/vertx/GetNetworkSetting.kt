package org.start2do.vertx

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.start2do.vertx.config.NetWorkSetting

/**
 * @Author Lijie
 * @date  2021/12/4:14:16
 */
interface GetNetworkSetting {
  fun get(vertx: Vertx, config: NetWorkSetting): Future<JsonObject>
}
