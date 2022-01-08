/*  大道至简 (C)2021 */
package org.start2do.vertx.utils

import org.start2do.utils.ResourceUtil
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths

/**
 * @Author HelloBox@outlook.com
 * @date 2020/10/20:20:47
 */
object ConfigFileReadUtil {
  fun read(fileName: String): InputStream {
    return try {
      if (Paths.get(fileName).toFile().exists()) {
        ResourceUtil.getFileInputStream(fileName)!!
      } else {
        ResourceUtil.getFileInputStream("classpath*:$fileName")!!
      }
    } catch (e: Exception) {
      val property = System.getenv("resourcesDir")
      if (property.isNullOrEmpty()) {
        throw FileNotFoundException()
      }
      ResourceUtil.getFileInputStream("$property/$fileName")!!
    }
  }
}
