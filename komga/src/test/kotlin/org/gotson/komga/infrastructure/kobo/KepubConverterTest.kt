package org.gotson.komga.infrastructure.kobo

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.makeBook
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile

@SpringBootTest
class KepubConverterTest(
  @Autowired private val kepubConverter: KepubConverter,
) {
  @BeforeAll
  fun setup(
    @TempDir tmpDir: Path,
  ) {
    tmpDir.resolve("kepubify").apply {
      createFile()
      toFile().setExecutable(true)
      kepubConverter.configureKepubify(this.toAbsolutePath().toString())
    }
  }

  @Test
  fun `given kepub book when converting then IllegalArgument is thrown`(
    @TempDir dir: Path,
  ) {
    // given
    val book = makeBook("book", url = dir.resolve("book.epub").toUri().toURL())
    val media = Media(mediaType = MediaType.EPUB.type, epubIsKepub = true)

    // when
    val thrownBy = catchThrowable { kepubConverter.convertEpubToKepub(BookWithMedia(book, media), dir) }

    // then
    assertThat(thrownBy).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given non-EPUB book when converting then IllegalArgument is thrown`(
    @TempDir dir: Path,
  ) {
    // given
    val book = makeBook("book", url = dir.resolve("book.epub").toUri().toURL())
    val media = Media(mediaType = MediaType.ZIP.type)

    // when
    val thrownBy = catchThrowable { kepubConverter.convertEpubToKepub(BookWithMedia(book, media), dir) }

    // then
    assertThat(thrownBy).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given non-existent file when converting then IllegalArgument is thrown`(
    @TempDir dir: Path,
  ) {
    // given
    val book = makeBook("book", url = dir.resolve("book.epub").toUri().toURL())
    val media = Media(mediaType = MediaType.EPUB.type)

    // when
    val thrownBy = catchThrowable { kepubConverter.convertEpubToKepub(BookWithMedia(book, media), dir) }

    // then
    assertThat(thrownBy).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given existing book file and dummy kepubify when converting then conversion fails`(
    @TempDir dir: Path,
  ) {
    // given
    val source = Files.createTempFile(dir, "book", ".epub")

    val book = makeBook("book", url = source.toUri().toURL())
    val media = Media(mediaType = MediaType.EPUB.type)

    // when
    val result = kepubConverter.convertEpubToKepub(BookWithMedia(book, media), dir)

    // then
    assertThat(result).isNull()
  }
}
