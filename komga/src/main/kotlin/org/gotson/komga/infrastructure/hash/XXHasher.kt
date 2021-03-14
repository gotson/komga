package org.gotson.komga.infrastructure.hash

import mu.KotlinLogging
import org.apache.commons.codec.digest.XXHash32
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Component
class XXHasher {
  private val readBlockSize = 4096

  fun getHash(path: Path): String {
    logger.info { "Calculating hash for the file $path" }
    val file = path.toFile()
    val bytesToSkip = java.lang.Long.highestOneBit(file.length() / 100)

    with(FileInputStream(file)) {
      val hash32 = XXHash32()
      val buf = ByteArray(readBlockSize)

      while (true) {
        val read = this.read(buf)
        if (read == -1) {
          break
        }

        hash32.update(buf, 0, read)
        this.skip(bytesToSkip)
      }

      return hash32.value.toString(16)
    }
  }
}
