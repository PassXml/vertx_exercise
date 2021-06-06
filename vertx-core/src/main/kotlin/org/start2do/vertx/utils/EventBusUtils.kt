/*  大道至简 (C)2020 */
package org.start2do.vertx.utils

import org.start2do.api.AbsService
import org.start2do.vertx.Global
import org.start2do.vertx.sys.ServiceManager

/**
 * @Author HelloBox@outlook.com
 * @date 2020/10/12:20:22
 */
inline fun <reified T : AbsService> getProxy(address: String = (System.getProperty(Global.SERVICE_ADDRESS))): T {
  return ServiceManager.getServiceProxyBuilder().setAddress("$address.${T::class.java.simpleName}").build(T::class.java)
}
