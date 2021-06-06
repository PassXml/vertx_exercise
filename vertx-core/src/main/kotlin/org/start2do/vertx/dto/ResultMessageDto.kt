/*  大道至简 (C)2020 */
package org.start2do.vertx.dto

/**
 * @Author HelloBox@outlook.com
 * @date 2020/8/20:18:00
 */

data class ResultMessageDto<T>(
  val code: Int = 0,
  val msg: String? = null,
  val result: T? = null
) {
  companion object {
    fun build(msg: String?): ResultMessageDto<Any> {
      return ResultMessageDto<Any>(
        -1,
        msg,
        null
      )
    }

    fun build(code: Int, msg: String?): ResultMessageDto<Any> {
      return ResultMessageDto(
        code,
        msg,
        null
      )
    }

    fun <T> build(code: Int, obj: T): ResultMessageDto<T> {
      return ResultMessageDto<T>(
        code,
        null,
        obj
      )
    }

    fun <T> build(obj: T): ResultMessageDto<T> {
      return ResultMessageDto<T>(
        0,
        null,
        obj
      )
    }
  }
}
