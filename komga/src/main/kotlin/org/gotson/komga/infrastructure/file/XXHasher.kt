package org.gotson.komga.infrastructure.file

import net.jpountz.xxhash.XXHashFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class XXHasher : FileHasher {
  private val factory: XXHashFactory = XXHashFactory.fastestInstance()
  private val seed = -0x68b84d74L

  override fun getHash(path: Path): String {
    with(FileSystemResource(path)) {
      val hash64 = factory.newStreamingHash64(seed)
      val buf = ByteArray(8192)
      val inputStream = this.inputStream

      while (true) {
        val read: Int = inputStream.read(buf)
        if (read == -1) {
          break
        }
        hash64.update(buf, 0, read)
      }

      return hash64.value.toString(16)
    }
  }
}
