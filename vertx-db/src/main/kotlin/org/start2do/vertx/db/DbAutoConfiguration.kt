/*  大道至简 (C)2021 */
package org.start2do.vertx.db

import org.start2do.vertx.pojo.AutoConfiguration

/**
 * @Author passxml
 * @date 2021/5/30:14:31
 */
class DbAutoConfiguration : AutoConfiguration {
  override fun getScanPackages(): Array<String> = arrayOf("org.start2do.vertx.db")
}
