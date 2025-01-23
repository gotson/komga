package org.gotson.komga.infrastructure.util

import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.EntryNotFoundException
import java.io.InputStream
import java.nio.file.Path

inline fun <R> ZipFile.Builder.use(block: (ZipFile) -> R) = this.get().use(block)

fun ZipFile.getEntryInputStream(entryName: String): InputStream? = this.getEntry(entryName)?.let { entry -> this.getInputStream(entry) }

fun ZipFile.getEntryBytes(entryName: String): ByteArray? = this.getEntry(entryName)?.let { entry -> this.getInputStream(entry).use { it.readBytes() } }

fun getZipEntryBytes(
  path: Path,
  entryName: String,
): ByteArray {
  // fast path. Only read central directory record and try to find entry in it
  val zipBuilder =
    ZipFile
      .builder()
      .setPath(path)
      .setUseUnicodeExtraFields(true)
      .setIgnoreLocalFileHeader(true)
  val bytes = zipBuilder.use { it.getEntryBytesClosing(entryName) }
  if (bytes != null) return bytes

  // slow path. Entry with that name wasn't in central directory record
  // Iterate each entry and, if present, set name from Unicode extra field in local file header
  return zipBuilder.setIgnoreLocalFileHeader(false).use {
    it.getEntryBytesClosing(entryName)
      ?: throw EntryNotFoundException("Entry does not exist: $entryName")
  }
}

private fun ZipFile.getEntryBytesClosing(entryName: String) =
  this.use { zip ->
    zip.getEntry(entryName)?.let { entry ->
      zip.getInputStream(entry).use { it.readBytes() }
    }
  }
