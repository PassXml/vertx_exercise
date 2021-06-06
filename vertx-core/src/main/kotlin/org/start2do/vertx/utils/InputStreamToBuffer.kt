/*  大道至简 (C)2020 */
package org.start2do.vertx.utils

import io.vertx.core.buffer.Buffer
import java.io.InputStream

/**
 * @Author HelloBox@outlook.com
 * @date 2020/7/7:19:36
 */
object InputStreamToBuffer {
  fun inputStreamToBuffer(input: InputStream): Buffer? {
    val data = ByteArray(1024)
    val buffer: Buffer = Buffer.buffer()
    while (input.read(data, 0, data.size) != -1) {
      buffer.appendBytes(data)
    }
    return buffer
  }
}
