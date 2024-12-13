package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.toPath

@SpringBootTest
class BookAnalyzerTest(
  @Autowired private val komgaProperties: KomgaProperties,
) {
  @SpykBean
  private lateinit var bookAnalyzer: BookAnalyzer

  @Test
  fun `given rar4 archive when analyzing then media status is READY`() {
    val file = ClassPathResource("archives/rar4.rar")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=4")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(3)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "rar4-solid.rar", "rar4-encrypted.rar",
    ],
  )
  fun `given rar4 solid or encrypted archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/rar4-solid.rar")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=4")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "rar5.rar", "rar5-solid.rar", "rar5-encrypted.rar",
    ],
  )
  fun `given rar5 archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=5")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "7zip.7z", "7zip-encrypted.7z",
    ],
  )
  fun `given 7zip archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/x-7z-compressed")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "zip.zip", "zip-bzip2.zip", "zip-copy.zip", "zip-deflate64.zip", "zip-lzma.zip", "zip-ppmd.zip",
    ],
  )
  fun `given zip archive when analyzing then media status is READY`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/zip")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(1)
  }

  @Test
  fun `given zip encrypted archive when analyzing then media status is ERROR`() {
    val file = ClassPathResource("archives/zip-encrypted.zip")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/zip")
    assertThat(media.status).isEqualTo(Media.Status.ERROR)
  }

  @Test
  fun `given epub archive when analyzing then media status is READY`() {
    val file = ClassPathResource("archives/epub3.epub")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/epub+zip")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(0)
  }

  @Test
  fun `given broken epub archive when analyzing then media status is ERROR`() {
    val file = ClassPathResource("archives/zip-as-epub.epub")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book, false)

    assertThat(media.mediaType).isEqualTo("application/zip")
    assertThat(media.status).isEqualTo(Media.Status.ERROR)
    assertThat(media.pages).hasSize(0)
  }

  @Test
  fun `given book with a single page when hashing then all pages are hashed`() {
    val book = makeBook("book1")
    val pages = listOf(BookPage("1.jpeg", "image/jpeg"))
    val media = Media(Media.Status.READY, pages = pages)

    every { bookAnalyzer.getPageContent(any(), any()) } returns ByteArray(1)
    every { bookAnalyzer.hashPage(any(), any()) } returns "hashed"

    val hashedMedia = bookAnalyzer.hashPages(BookWithMedia(book, media))

    assertThat(hashedMedia.pages).hasSize(1)
    assertThat(hashedMedia.pages.first().fileHash).isEqualTo("hashed")
  }

  @Test
  fun `given book with more than 6 pages when hashing then only first and last 3 are hashed`() {
    val book = makeBook("book1")
    val pages = (1..30).map { BookPage("$it.jpeg", "image/jpeg") }
    val media = Media(Media.Status.READY, pages = pages)

    every { bookAnalyzer.getPageContent(any(), any()) } returns ByteArray(1)
    every { bookAnalyzer.hashPage(any(), any()) } returns "hashed"

    val hashedMedia = bookAnalyzer.hashPages(BookWithMedia(book, media))

    assertThat(hashedMedia.pages).hasSize(30)
    assertThat(hashedMedia.pages.take(komgaProperties.pageHashing).map { it.fileHash })
      .hasSize(komgaProperties.pageHashing)
      .containsOnly("hashed")
    assertThat(hashedMedia.pages.takeLast(komgaProperties.pageHashing).map { it.fileHash })
      .hasSize(komgaProperties.pageHashing)
      .containsOnly("hashed")
    assertThat(
      hashedMedia.pages
        .drop(komgaProperties.pageHashing)
        .dropLast(komgaProperties.pageHashing)
        .map { it.fileHash },
    ).hasSize(30 - (komgaProperties.pageHashing * 2))
      .containsOnly("")
  }

  @Test
  fun `given book with already hashed pages when hashing then no hashing is done`() {
    val book = makeBook("book1")
    val pages = (1..30).map { BookPage("$it.jpeg", "image/jpeg", fileHash = "hashed") }
    val media = Media(Media.Status.READY, pages = pages)

    val hashedMedia = bookAnalyzer.hashPages(BookWithMedia(book, media))

    verify(exactly = 0) { bookAnalyzer.getPageContent(any(), any()) }
    verify(exactly = 0) { bookAnalyzer.hashPage(any(), any()) }

    assertThat(hashedMedia.pages.map { it.fileHash })
      .hasSize(30)
      .containsOnly("hashed")
  }

  @ParameterizedTest
  @MethodSource("provideDirectoriesForPageHashing")
  fun `given 2 exact pages when hashing then hashes are the same`(directory: Path) {
    val files = directory.listDirectoryEntries()
    assertThat(files).hasSize(2)

    val mediaType = "image/${directory.fileName.extension}"

    val hashes =
      files.map {
        bookAnalyzer.hashPage(BookPage(it.name, mediaType = mediaType), it.inputStream().readBytes())
      }

    assertThat(hashes.first()).isEqualTo(hashes.last())
  }

  companion object {
    @JvmStatic
    fun provideDirectoriesForPageHashing() = ClassPathResource("hashpage").uri.toPath().listDirectoryEntries()
  }
}
