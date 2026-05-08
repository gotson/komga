package org.gotson.komga.infrastructure.mediacontainer

import com.chpark.crypto.CryptoEngine
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.stereotype.Service
import java.nio.file.Path
import org.gotson.komga.infrastructure.util.getZipEntryBytes as getZipEntryBytesFromArchive

@Service
class MediaFileDecryptionService(
  private val komgaProperties: KomgaProperties,
) {
  private val cryptoEngine = CryptoEngine()

  private val password: String?
    get() = komgaProperties.mediaFileDecryption.password?.takeIf { it.isNotBlank() }

  fun getZipEntryBytes(
    path: Path,
    entryName: String,
  ): ByteArray =
    decryptZipImageEntry(
      entryName,
      getZipEntryBytesFromArchive(path, entryName),
    )

  fun getEpubEntryBytes(
    path: Path,
    entryName: String,
  ): ByteArray =
    decryptEpubEntry(
      entryName,
      getZipEntryBytesFromArchive(path, entryName),
    )

  fun decryptEpubEntry(
    entryName: String,
    bytes: ByteArray,
  ): ByteArray =
    decryptEntryIfNeeded(
      entryName,
      bytes,
      EPUB_HTML_EXTENSIONS,
      "Encrypted EPUB entry password is not configured",
    )

  private fun decryptZipImageEntry(
    entryName: String,
    bytes: ByteArray,
  ): ByteArray =
    decryptEntryIfNeeded(
      entryName,
      bytes,
      ZIP_IMAGE_EXTENSIONS,
      "Encrypted ZIP image entry password is not configured",
    )

  private fun decryptEntryIfNeeded(
    entryName: String,
    bytes: ByteArray,
    encryptedExtensions: Set<String>,
    missingPasswordMessage: String,
  ): ByteArray {
    if (entryName.substringAfterLast('.', "").lowercase() !in encryptedExtensions) return bytes
    if (!CryptoEngine.hasMagicHeader(bytes)) return bytes

    val password = password ?: throw IllegalStateException(missingPasswordMessage)
    return cryptoEngine.decrypt(password, bytes)
  }

  private companion object {
    private val EPUB_HTML_EXTENSIONS = setOf("html", "xhtml")
    private val ZIP_IMAGE_EXTENSIONS = setOf("avif", "bmp", "gif", "jpe", "jpeg", "jpg", "jxl", "png", "tif", "tiff", "webp")
  }
}
