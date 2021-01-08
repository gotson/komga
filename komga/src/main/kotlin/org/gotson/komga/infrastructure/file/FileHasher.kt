package org.gotson.komga.infrastructure.file

import java.nio.file.Path

interface FileHasher {
  fun getHash(path: Path): String
}
