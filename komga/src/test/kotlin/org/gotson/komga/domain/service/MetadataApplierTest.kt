package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.WebLink
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

class MetadataApplierTest {
  private val metadataApplier = MetadataApplier()

  @Nested
  inner class Book {
    @Test
    fun `given locked metadata when applying patch then metadata is not changed`() {
      val metadata =
        BookMetadata(
          title = "title",
          number = "1",
          numberSort = 1F,
          titleLock = true,
          summaryLock = true,
          numberLock = true,
          numberSortLock = true,
          releaseDateLock = true,
          authorsLock = true,
          tagsLock = true,
          isbnLock = true,
          linksLock = true,
        )

      val patch =
        BookMetadataPatch(
          title = "new title",
          summary = "new summary",
          number = "2",
          numberSort = 2F,
          releaseDate = LocalDate.of(2020, 12, 2),
          authors = listOf(Author("Marcel", "writer")),
          isbn = "9782811632397",
          links = listOf(WebLink("Comixology", URI("https://www.comixology.com/Sandman/digital-comic/727888"))),
          tags = setOf("tag1", "tag2"),
        )

      val patched = metadataApplier.apply(patch, metadata)

      assertThat(patched.title).isEqualTo(metadata.title)
      assertThat(patched.number).isEqualTo(metadata.number)
      assertThat(patched.numberSort).isEqualTo(metadata.numberSort)
      assertThat(patched.summary).isEqualTo("")
      assertThat(patched.authors).isEmpty()
      assertThat(patched.releaseDate).isNull()
      assertThat(patched.tags).isEmpty()
      assertThat(patched.isbn).isEqualTo("")
      assertThat(patched.links).isEmpty()
      assertThat(patched.tags).isEmpty()
    }

    @Test
    fun `given unlocked metadata when applying patch then metadata is changed`() {
      val metadata =
        BookMetadata(
          title = "title",
          number = "1",
          numberSort = 1F,
        )

      val patch =
        BookMetadataPatch(
          title = "new title",
          summary = "new summary",
          number = "2",
          numberSort = 2F,
          releaseDate = LocalDate.of(2020, 12, 2),
          authors = listOf(Author("Marcel", "writer")),
          isbn = "9782811632397",
          links = listOf(WebLink("Comixology", URI("https://www.comixology.com/Sandman/digital-comic/727888"))),
          tags = setOf("tag1", "tag2"),
        )

      val patched = metadataApplier.apply(patch, metadata)

      assertThat(patched.title).isEqualTo(patch.title)
      assertThat(patched.number).isEqualTo(patch.number)
      assertThat(patched.numberSort).isEqualTo(patch.numberSort)
      assertThat(patched.summary).isEqualTo(patch.summary)
      assertThat(patched.authors)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Author("Marcel", "writer"),
        )
      assertThat(patched.releaseDate).isEqualTo(patch.releaseDate)
      assertThat(patched.isbn).isEqualTo(patch.isbn)
      assertThat(patched.links)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          WebLink("Comixology", URI("https://www.comixology.com/Sandman/digital-comic/727888")),
        )
      assertThat(patched.tags as Iterable<String>)
        .hasSize(2)
        .containsExactlyInAnyOrder("tag1", "tag2")
    }
  }

  @Nested
  inner class Series {
    @Test
    fun `given locked metadata when applying patch then metadata is not changed`() {
      val metadata =
        SeriesMetadata(
          title = "title",
          statusLock = true,
          titleLock = true,
          titleSortLock = true,
          summaryLock = true,
          readingDirectionLock = true,
          publisherLock = true,
          ageRatingLock = true,
          languageLock = true,
          genresLock = true,
          tagsLock = true,
          totalBookCountLock = true,
        )

      val patch =
        SeriesMetadataPatch(
          title = "new title",
          titleSort = "new title sort",
          status = SeriesMetadata.Status.ENDED,
          summary = "new summary",
          readingDirection = SeriesMetadata.ReadingDirection.VERTICAL,
          publisher = "new publisher",
          ageRating = 12,
          language = "en",
          genres = setOf("shonen"),
          totalBookCount = 12,
          collections = emptySet(),
        )

      val patched = metadataApplier.apply(patch, metadata)

      assertThat(patched.title).isEqualTo(metadata.title)
      assertThat(patched.titleSort).isEqualTo(metadata.titleSort)
      assertThat(patched.status).isEqualTo(metadata.status)
      assertThat(patched.summary).isEqualTo(metadata.summary)
      assertThat(patched.readingDirection).isEqualTo(metadata.readingDirection)
      assertThat(patched.publisher).isEqualTo(metadata.publisher)
      assertThat(patched.ageRating).isEqualTo(metadata.ageRating)
      assertThat(patched.language).isEqualTo(metadata.language)
      assertThat(patched.genres).isEmpty()
      assertThat(patched.totalBookCount).isNull()
      assertThat(patched.tags).isEmpty()
    }

    @Test
    fun `given unlocked metadata when applying patch then metadata is changed`() {
      val metadata =
        SeriesMetadata(
          title = "title",
        )

      val patch =
        SeriesMetadataPatch(
          title = "new title",
          titleSort = "new title sort",
          status = SeriesMetadata.Status.ENDED,
          summary = "new summary",
          readingDirection = SeriesMetadata.ReadingDirection.VERTICAL,
          publisher = "new publisher",
          ageRating = 12,
          language = "en",
          genres = setOf("shonen"),
          totalBookCount = 12,
          collections = emptySet(),
        )

      val patched = metadataApplier.apply(patch, metadata)

      assertThat(patched.title).isEqualTo(patch.title)
      assertThat(patched.titleSort).isEqualTo(patch.titleSort)
      assertThat(patched.status).isEqualTo(patch.status)
      assertThat(patched.summary).isEqualTo(patch.summary)
      assertThat(patched.readingDirection).isEqualTo(patch.readingDirection)
      assertThat(patched.publisher).isEqualTo(patch.publisher)
      assertThat(patched.ageRating).isEqualTo(patch.ageRating)
      assertThat(patched.language).isEqualTo(patch.language)
      assertThat(patched.totalBookCount).isEqualTo(patch.totalBookCount)
      assertThat(patched.genres as Iterable<String>)
        .hasSize(1)
        .containsExactlyInAnyOrder("shonen")
      assertThat(patched.tags).isEmpty()
    }
  }
}
