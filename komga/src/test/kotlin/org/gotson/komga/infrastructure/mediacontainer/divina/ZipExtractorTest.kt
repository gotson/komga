package org.gotson.komga.infrastructure.mediacontainer.divina

import com.chpark.crypto.CryptoEngine
import org.apache.tika.config.TikaConfig
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.image.ImageAnalyzer
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

class ZipExtractorTest {
  private val contentDetector = ContentDetector(TikaConfig())
  private val imageAnalyzer = ImageAnalyzer()
  private val komgaProperties = KomgaProperties()
  private val zipExtractor = ZipExtractor(contentDetector, imageAnalyzer, MediaFileDecryptionService(komgaProperties))

  @Test
  fun `given zip file when parsing for entries then returns all images`() {
    val fileResource = ClassPathResource("archives/zip.zip")

    val entries = zipExtractor.getEntries(fileResource.file.toPath(), true)

    assertThat(entries).hasSize(1)
    with(entries.first()) {
      assertThat(name).isEqualTo("komga.png")
      assertThat(mediaType).isEqualTo("image/png")
      assertThat(dimension).isEqualTo(Dimension(48, 48))
      assertThat(fileSize).isEqualTo(3108)
    }
  }

  @Test
  fun `given zip file when parsing for entries without analyzing dimensions then returns all images without dimensions`() {
    val fileResource = ClassPathResource("archives/zip.zip")

    val entries = zipExtractor.getEntries(fileResource.file.toPath(), false)

    assertThat(entries).hasSize(1)
    with(entries.first()) {
      assertThat(name).isEqualTo("komga.png")
      assertThat(mediaType).isEqualTo("image/png")
      assertThat(dimension).isNull()
      assertThat(fileSize).isEqualTo(3108)
    }
  }

  @Test
  fun `given zip with encrypted image entry when parsing for entries then decrypts requested image bytes`(
    @TempDir tempDir: Path,
  ) {
    val password = "secret"
    komgaProperties.mediaFileDecryption.password = password
    val encrypted = tempDir.resolve("encrypted.cbz")
    encryptZipImageEntries(ClassPathResource("archives/zip.zip").file.toPath(), encrypted, password)

    val entries = zipExtractor.getEntries(encrypted, true)
    val entryBytes = zipExtractor.getEntryStream(encrypted, "komga.png")

    assertThat(entries).hasSize(1)
    with(entries.first()) {
      assertThat(name).isEqualTo("komga.png")
      assertThat(mediaType).isEqualTo("image/png")
      assertThat(dimension).isEqualTo(Dimension(48, 48))
      assertThat(fileSize).isEqualTo(3108)
    }
    assertThat(entryBytes).isEqualTo(
      ClassPathResource("archives/zip.zip").file.toPath().let { path ->
        ZipInputStream(path.inputStream()).use { zipIn ->
          zipIn.nextEntry
          zipIn.readBytes()
        }
      },
    )
  }

  private fun encryptZipImageEntries(
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
            zipOut.write(if (entry.name == "komga.png") cryptoEngine.encrypt(password, bytes) else bytes)
          }
          zipOut.closeEntry()
          zipIn.closeEntry()
        }
      }
    }
  }
}
