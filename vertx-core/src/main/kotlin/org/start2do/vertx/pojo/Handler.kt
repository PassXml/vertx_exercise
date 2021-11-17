package org.start2do.vertx.pojo

/**
 * @Author Lijie
 * @date  2021/11/16:15:38
 */
interface Handler<T, R> {
  fun handle(t: T): R
}
