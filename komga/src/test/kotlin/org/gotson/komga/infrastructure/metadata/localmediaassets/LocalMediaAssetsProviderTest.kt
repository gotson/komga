package org.gotson.komga.infrastructure.metadata.localmediaassets

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.mockk.every
import io.mockk.spyk
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.TikaConfiguration
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

class LocalMediaAssetsProviderTest {

  private val contentDetector = spyk(ContentDetector(TikaConfiguration().tika())).also {
    every { it.detectMediaType(any<Path>()) } answers {
      when (FilenameUtils.getExtension(firstArg<Path>().toString().toLowerCase())) {
        "jpg", "jpeg", "tbn" -> "image/jpeg"
        "png" -> "image/png"
        else -> "application/octet-stream"
      }
    }
  }

  private val localMediaAssetsProvider = LocalMediaAssetsProvider(contentDetector)

  @Test
  fun `given root directory with only files when scanning then return 1 series containing those files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val bookFile = Files.createFile(root.resolve("book(e).cbz"))
      val thumbsFiles = listOf("bOOk(e).jpeg", "Book(e).tbn", "book(e).PNG", "book(e).jpeg")
      val thumbsDashFiles = listOf("book(e)-1.jpeg", "book(e)-2.tbn", "book(e)-23.png", "book(e)-111.jpeg")
      val invalidFiles = listOf("book12(e).jpeg", "book(e).gif", "cover.png", "other.jpeg")

      (thumbsFiles + thumbsDashFiles + invalidFiles).forEach { Files.createFile(root.resolve(it)) }

      val book = spyk(Book(
        name = "Book",
        url = bookFile.toUri().toURL(),
        fileLastModified = LocalDateTime.now()
      ))
      every { book.path() } returns bookFile

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
}
