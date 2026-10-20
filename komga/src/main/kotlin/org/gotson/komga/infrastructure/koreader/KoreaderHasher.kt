package org.gotson.komga.infrastructure.koreader

import com.appmattus.crypto.Algorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.RandomAccessFile
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Component
class KoreaderHasher {
  fun computeHash(path: Path): String {
    logger.debug { "Koreader hashing: $path" }

    return partialMd5(path)
  }

  /**
   * From https://github.com/koreader/koreader/blob/5bd3f3b42c95fd143d98f8fc9695d486fd92b7c8/frontend/util.lua#L1093-L1119
   */
  private fun partialMd5(path: Path): String {
    val step = 1024L
    val size = 1024
    val digest = Algorithm.MD5.createDigest()

    val file = RandomAccessFile(path.toFile(), "r")

    val buffer = ByteArray(size)
    (-1..10).forEach {
      file.seek(step shl (2 * it))
      val size = file.read(buffer)
      if (size > 0) digest.update(buffer, 0, size)
    }

    return digest.digest().toHexString()
  }
}
