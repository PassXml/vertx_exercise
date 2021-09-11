/*  大道至简 (C)2021 */
package org.start2do.vertx.pojo

import io.vertx.serviceproxy.ServiceException

/**
 * @Author HelloBox@outlook.com
 * @date 2020/10/20:22:40
 */
class BusinessException(var failureCode: Int, message: String = "") : ServiceException(failureCode, message)
