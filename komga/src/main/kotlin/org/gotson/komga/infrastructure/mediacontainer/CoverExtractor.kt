package org.gotson.komga.infrastructure.mediacontainer

import java.nio.file.Path

interface CoverExtractor {
  fun getCoverStream(path: Path): ByteArray?
}
