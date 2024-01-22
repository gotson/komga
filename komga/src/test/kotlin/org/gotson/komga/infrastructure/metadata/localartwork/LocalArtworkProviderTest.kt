package org.gotson.komga.infrastructure.metadata.localartwork

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.mockk.every
import io.mockk.spyk
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.TikaConfiguration
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.extension

class LocalArtworkProviderTest {
  private val contentDetector =
    spyk(ContentDetector(TikaConfiguration().tika())).also {
      every { it.detectMediaType(any<Path>()) } answers {
        when (firstArg<Path>().extension.lowercase()) {
          "jpg", "jpeg", "tbn" -> "image/jpeg"
          "png" -> "image/png"
          "webp" -> "image/webp"
          "avif" -> "image/avif"
          "jxl" -> "image/jxl"
          else -> "application/octet-stream"
        }
      }
    }

  private val localMediaAssetsProvider = LocalArtworkProvider(contentDetector, ImageAnalyzer())

  @Test
  fun `given book with sidecar files when getting thumbnails then return valid ones`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val bookFile = Files.createFile(root.resolve("book(e).cbz"))
      val thumbsFiles = listOf("bOOk(e).jpeg", "Book(e).tbn", "book(e).PNG", "book(e).jpeg", "book(e).webp")
      val thumbsDashFiles = listOf("book(e)-1.jpeg", "book(e)-2.tbn", "book(e)-23.png", "book(e)-111.jpeg", "book(e)-123.webp")
      val invalidFiles = listOf("book12(e).jpeg", "book(e).gif", "cover.png", "other.jpeg", "book.webp", "book(e).avif", "book(e).jxl")

      (thumbsFiles + thumbsDashFiles + invalidFiles).forEach { Files.createFile(root.resolve(it)) }

      val book =
        spyk(
          Book(
            name = "Book",
            url = bookFile.toUri().toURL(),
            fileLastModified = LocalDateTime.now(),
          ),
        )
      every { book.path } returns bookFile

      // when
      val thumbnails = localMediaAssetsProvider.getBookThumbnails(book)

      // then
      assertThat(thumbnails).hasSize(thumbsFiles.size + thumbsDashFiles.size)
      assertThat(thumbnails.filter { it.selected }).hasSize(1)
      assertThat(thumbnails.map { FilenameUtils.getName(it.url.toString()) })
        .containsAll(thumbsFiles)
        .containsAll(thumbsDashFiles)
        .doesNotContainAnyElementsOf(invalidFiles)
    }
  }

  @Test
  fun `given series with sidecar files when getting thumbnails then return valid ones`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val seriesPath = fs.getPath("/series")
      val seriesFile = Files.createDirectory(seriesPath)

      val thumbsFiles = listOf("CoVeR.jpeg", "DefauLt.tbn", "POSter.PNG", "FoLDer.jpeg", "serIES.TBN", "serIes.WebP")
      val invalidFiles = listOf("cover.gif", "artwork.jpg", "other.jpeg", "cover.avif", "series.jxl")

      (thumbsFiles + invalidFiles).forEach { Files.createFile(seriesPath.resolve(it)) }

      val series =
        spyk(
          Series(
            name = "Series",
            url = seriesFile.toUri().toURL(),
            fileLastModified = LocalDateTime.now(),
          ),
        )
      every { series.path } returns seriesFile

      // when
      val thumbnails = localMediaAssetsProvider.getSeriesThumbnails(series)

      // then
      assertThat(thumbnails).hasSize(thumbsFiles.size)
      assertThat(thumbnails.filter { it.selected }).hasSize(1)
      assertThat(thumbnails.map { FilenameUtils.getName(it.url.toString()) })
        .containsAll(thumbsFiles)
        .doesNotContainAnyElementsOf(invalidFiles)
    }
  }
}
