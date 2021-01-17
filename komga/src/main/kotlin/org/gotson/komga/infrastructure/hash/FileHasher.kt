package org.gotson.komga.infrastructure.hash

import java.nio.file.Path

interface FileHasher {
  fun getHash(path: Path): String
}
