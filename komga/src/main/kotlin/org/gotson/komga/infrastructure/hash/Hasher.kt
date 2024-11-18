package org.gotson.komga.infrastructure.hash

import com.appmattus.crypto.Algorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

private const val DEFAULT_BUFFER_SIZE = 8192
private const val SEED = 0

@Component
class Hasher {
  fun computeHash(path: Path): String {
    logger.debug { "Hashing: $path" }

    return computeHash(path.inputStream())
  }

  fun computeHash(stream: InputStream): String {
    val hash = Algorithm.XXH3_128.Seeded(SEED.toLong()).createDigest()

    stream.use {
      val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
      var len: Int

      do {
        len = it.read(buffer)
        if (len >= 0) hash.update(buffer, 0, len)
      } while (len >= 0)
    }

    return hash.digest().toHexString()
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  fun ByteArray.toHexString(): String =
    asUByteArray().joinToString("") {
      it.toString(16).padStart(2, '0')
    }
}
