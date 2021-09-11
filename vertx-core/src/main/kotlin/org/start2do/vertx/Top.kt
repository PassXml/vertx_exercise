/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.WorkerExecutor
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.core.spi.logging.LogDelegate
import io.vertx.core.spi.logging.LogDelegateFactory
import kotlinx.coroutines.CoroutineDispatcher
import org.start2do.vertx.ext.CCoroutineExceptionHandler
import org.start2do.vertx.ext.getLogger
import kotlin.properties.Delegates

/**
 * @Author HelloBox@outlook.com
 * @date 2020/9/20:13:34
 */
object Top {
  lateinit var Breaker: CircuitBreaker
  var TopClusterManager: ClusterManager? = null
  lateinit var LongTimeExecutor: WorkerExecutor
  val logger: LogDelegate = getLogger("TOP")
  val CCoroutineExceptionHandler = CCoroutineExceptionHandler()
  lateinit var coroutineDispatcher: CoroutineDispatcher
  lateinit var logDelegate: LogDelegateFactory;
}
