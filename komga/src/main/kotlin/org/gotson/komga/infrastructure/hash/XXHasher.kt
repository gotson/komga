package org.gotson.komga.infrastructure.hash

import mu.KotlinLogging
import net.jpountz.xxhash.XXHashFactory
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Component
class XXHasher : FileHasher {
  private val factory: XXHashFactory = XXHashFactory.fastestInstance()
  private val seed = -0x68b84d74L
  private val readBlockSize = 4096

  override fun getHash(path: Path): String {
    logger.debug { "calculating hash for the file ${path.toUri()}" }
    val file = path.toFile()
    val bytesToSkip = java.lang.Long.highestOneBit(file.length() / 100)

    with(FileInputStream(file)) {
      val hash64 = factory.newStreamingHash64(seed)
      val buf = ByteArray(readBlockSize)

      while (true) {
        val read = this.read(buf)
        if (read == -1) {
          break
        }
        hash64.update(buf, 0, read)

        this.skip(bytesToSkip)
      }

      return hash64.value.toString(16)
    }
  }
}
