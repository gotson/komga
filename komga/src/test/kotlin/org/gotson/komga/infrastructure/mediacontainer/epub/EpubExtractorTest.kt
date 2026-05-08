package org.gotson.komga.infrastructure.mediacontainer.epub

import com.chpark.crypto.CryptoEngine
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.kobo.KepubConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaFileDecryptionService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.ClassPathResource
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class EpubExtractorTest {
  private val komgaProperties = KomgaProperties()
  private val epubExtractor =
    EpubExtractor(
      mockk<ContentDetector>(),
      mockk<ImageAnalyzer>(),
      mockk<KepubConverter>(),
      MediaFileDecryptionService(komgaProperties),
      15,
    )

  @Test
  fun `given epub file when getting package file content then opf is returned`() {
    val fileResource = ClassPathResource("epub/The Incomplete Theft - Ralph Burke.epub")

    val packageFileContent = epubExtractor.getPackageFileContent(fileResource.file.toPath())

    assertThat(packageFileContent).contains("<dc:title id=\"id\">The Incomplete Theft</dc:title>")
  }

  @Test
  fun `given epub with encrypted html entry when getting entry stream then html is decrypted`(
    @TempDir tempDir: Path,
  ) {
    val password = "secret"
    komgaProperties.mediaFileDecryption.password = password
    val encrypted = tempDir.resolve("encrypted.epub")
    val entryName = "OEBPS/@public@vhost@g@gutenberg@html@files@65659@65659-h@65659-h-0.htm_split_000.html"
    encryptEpubHtmlEntries(ClassPathResource("epub/The Incomplete Theft - Ralph Burke.epub").file.toPath(), encrypted, password)

    val bytes = epubExtractor.getEntryStream(encrypted, entryName)
    val packageFileContent = epubExtractor.getPackageFileContent(encrypted)

    assertThat(bytes.decodeToString()).contains("<html")
    assertThat(packageFileContent).contains("<dc:title id=\"id\">The Incomplete Theft</dc:title>")
  }

  private fun encryptEpubHtmlEntries(
    input: Path,
    output: Path,
    password: String,
  ) {
    val cryptoEngine = CryptoEngine()
    ZipInputStream(input.inputStream()).use { zipIn ->
      ZipOutputStream(output.outputStream()).use { zipOut ->
        while (true) {
          val entry = zipIn.nextEntry ?: break
          zipOut.putNextEntry(ZipEntry(entry.name))
          if (!entry.isDirectory) {
            val bytes = zipIn.readBytes()
            zipOut.write(if (entry.name.isEpubHtmlEntry()) cryptoEngine.encrypt(password, bytes) else bytes)
          }
          zipOut.closeEntry()
          zipIn.closeEntry()
        }
      }
    }
  }

  private fun String.isEpubHtmlEntry(): Boolean {
    val lower = lowercase()
    return lower.endsWith(".xhtml") || lower.endsWith(".html")
  }
}
