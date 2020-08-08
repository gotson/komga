package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Media
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class BookAnalyzerTest(
  @Autowired private val bookAnalyzer: BookAnalyzer
) {

  @Test
  fun `given rar4 archive when analyzing then media status is READY`() {
    val file = ClassPathResource("archives/rar4.rar")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=4")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(3)
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "rar4-solid.rar", "rar4-encrypted.rar"
  ])
  fun `given rar4 solid or encrypted archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/rar4-solid.rar")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=4")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "rar5.rar", "rar5-solid.rar", "rar5-encrypted.rar"
  ])
  fun `given rar5 archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/x-rar-compressed; version=5")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "7zip.7z", "7zip-encrypted.7z"
  ])
  fun `given 7zip archive when analyzing then media status is UNSUPPORTED`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/x-7z-compressed")
    assertThat(media.status).isEqualTo(Media.Status.UNSUPPORTED)
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "zip.zip", "zip-bzip2.zip", "zip-copy.zip", "zip-deflate64.zip", "zip-lzma.zip", "zip-ppmd.zip"
  ])
  fun `given zip archive when analyzing then media status is READY`(fileName: String) {
    val file = ClassPathResource("archives/$fileName")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/zip")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(1)
  }

  @Test
  fun `given zip encrypted archive when analyzing then media status is ERROR`() {
    val file = ClassPathResource("archives/zip-encrypted.zip")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/zip")
    assertThat(media.status).isEqualTo(Media.Status.ERROR)
  }

  @Test
  fun `given epub archive when analyzing then media status is READY`() {
    val file = ClassPathResource("archives/epub3.epub")
    val book = Book("book", file.url, LocalDateTime.now())

    val media = bookAnalyzer.analyze(book)

    assertThat(media.mediaType).isEqualTo("application/epub+zip")
    assertThat(media.status).isEqualTo(Media.Status.READY)
    assertThat(media.pages).hasSize(1)
  }
}
