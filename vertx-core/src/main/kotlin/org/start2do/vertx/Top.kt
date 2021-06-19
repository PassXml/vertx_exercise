/*  大道至简 (C)2020 */
package org.start2do.vertx

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.WorkerExecutor
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.core.spi.logging.LogDelegate
import kotlinx.coroutines.CoroutineDispatcher
import org.start2do.vertx.ext.CCoroutineExceptionHandler

/**
 * @Author HelloBox@outlook.com
 * @date 2020/9/20:13:34
 */
object Top {
  lateinit var Breaker: CircuitBreaker
  var TopClusterManager: ClusterManager? = null
  lateinit var LongTimeExecutor: WorkerExecutor
  val logger: LogDelegate = Log4j2LogDelegateFactory().createDelegate("TOP")
  val CCoroutineExceptionHandler = CCoroutineExceptionHandler()
  lateinit var coroutineDispatcher: CoroutineDispatcher
}
