package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class SeriesMetadataDaoTest(
  @Autowired private val seriesMetadataDao: SeriesMetadataDao,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository
) {

  private val library = makeLibrary()

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
  }

  @AfterEach
  fun deleteSeries() {
    seriesRepository.findAll().forEach {
      seriesMetadataDao.delete(it.id)
    }
    seriesRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a seriesMetadata when inserting then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val now = LocalDateTime.now()
    val metadata = SeriesMetadata(
      status = SeriesMetadata.Status.ENDED,
      title = "Series",
      titleSort = "Series, The",
      summary = "Summary",
      readingDirection = SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT,
      publisher = "publisher",
      ageRating = 18,
      genres = setOf("Action", "Adventure"),
      tags = setOf("tag", "another"),
      language = "en",
      titleLock = true,
      titleSortLock = true,
      summaryLock = true,
      readingDirectionLock = true,
      publisherLock = true,
      ageRatingLock = true,
      genresLock = true,
      languageLock = true,
      tagsLock = true,
      seriesId = series.id
    )

    seriesMetadataDao.insert(metadata)
    val created = seriesMetadataDao.findById(metadata.seriesId)

    assertThat(created.seriesId).isEqualTo(series.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)

    assertThat(created.title).isEqualTo(metadata.title)
    assertThat(created.titleSort).isEqualTo(metadata.titleSort)
    assertThat(created.summary).isEqualTo(metadata.summary)
    assertThat(created.status).isEqualTo(SeriesMetadata.Status.ENDED)
    assertThat(created.readingDirection).isEqualTo(metadata.readingDirection)
    assertThat(created.publisher).isEqualTo(metadata.publisher)
    assertThat(created.ageRating).isEqualTo(metadata.ageRating)
    assertThat(created.language).isEqualTo(metadata.language)
    assertThat(created.genres).containsAll(metadata.genres)
    assertThat(created.tags).containsAll(metadata.tags)

    assertThat(created.titleLock).isEqualTo(metadata.titleLock)
    assertThat(created.titleSortLock).isEqualTo(metadata.titleSortLock)
    assertThat(created.statusLock).isEqualTo(metadata.statusLock)
    assertThat(created.summaryLock).isEqualTo(metadata.summaryLock)
    assertThat(created.readingDirectionLock).isEqualTo(metadata.readingDirectionLock)
    assertThat(created.publisherLock).isEqualTo(metadata.publisherLock)
    assertThat(created.ageRatingLock).isEqualTo(metadata.ageRatingLock)
    assertThat(created.genresLock).isEqualTo(metadata.genresLock)
    assertThat(created.languageLock).isEqualTo(metadata.languageLock)
    assertThat(created.tagsLock).isEqualTo(metadata.tagsLock)
  }

  @Test
  fun `given a minimum seriesMetadata when inserting then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val now = LocalDateTime.now()
    val metadata = SeriesMetadata(
      title = "Series",
      seriesId = series.id
    )

    seriesMetadataDao.insert(metadata)
    val created = seriesMetadataDao.findById(metadata.seriesId)

    assertThat(created.seriesId).isEqualTo(series.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)

    assertThat(created.title).isEqualTo(metadata.title)
    assertThat(created.titleSort).isEqualTo(metadata.title)
    assertThat(created.summary).isBlank()
    assertThat(created.status).isEqualTo(SeriesMetadata.Status.ONGOING)
    assertThat(created.readingDirection).isNull()
    assertThat(created.publisher).isBlank()
    assertThat(created.language).isBlank()
    assertThat(created.ageRating).isNull()
    assertThat(created.genres).isEmpty()
    assertThat(created.tags).isEmpty()

    assertThat(created.titleLock).isFalse()
    assertThat(created.titleSortLock).isFalse()
    assertThat(created.statusLock).isFalse()
    assertThat(created.summaryLock).isFalse()
    assertThat(created.readingDirectionLock).isFalse()
    assertThat(created.publisherLock).isFalse()
    assertThat(created.ageRatingLock).isFalse()
    assertThat(created.genresLock).isFalse()
    assertThat(created.languageLock).isFalse()
    assertThat(created.tagsLock).isFalse()
  }

  @Test
  fun `given existing seriesMetadata when finding by id then metadata is returned`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val metadata = SeriesMetadata(
      status = SeriesMetadata.Status.ENDED,
      title = "Series",
      titleSort = "Series, The",
      seriesId = series.id
    )

    seriesMetadataDao.insert(metadata)

    val found = seriesMetadataDao.findById(series.id)

    assertThat(found).isNotNull
    assertThat(found.title).isEqualTo("Series")
  }

  @Test
  fun `given non-existing seriesMetadata when finding by id then exception is thrown`() {
    val found = catchThrowable { seriesMetadataDao.findById("128742") }

    assertThat(found).isInstanceOf(Exception::class.java)
  }

  @Test
  fun `given non-existing seriesMetadata when findByIdOrNull then null is returned`() {
    val found = seriesMetadataDao.findByIdOrNull("128742")

    assertThat(found).isNull()
  }

  @Test
  fun `given a seriesMetadata when updating then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val metadata = SeriesMetadata(
      status = SeriesMetadata.Status.ENDED,
      title = "Series",
      titleSort = "Series, The",
      summary = "Summary",
      readingDirection = SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT,
      publisher = "publisher",
      ageRating = 18,
      language = "en",
      genres = setOf("Action"),
      tags = setOf("tag"),
      seriesId = series.id
    )
    seriesMetadataDao.insert(metadata)
    val created = seriesMetadataDao.findById(metadata.seriesId)

    val modificationDate = LocalDateTime.now()

    val updated = with(created) {
      copy(
        status = SeriesMetadata.Status.HIATUS,
        title = "Changed",
        titleSort = "Changed, The",
        summary = "SummaryUpdated",
        readingDirection = SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT,
        publisher = "publisher2",
        ageRating = 15,
        language = "jp",
        genres = setOf("Adventure"),
        tags = setOf("Another"),
        statusLock = true,
        titleLock = true,
        titleSortLock = true,
        summaryLock = true,
        readingDirectionLock = true,
        publisherLock = true,
        ageRatingLock = true,
        languageLock = true,
        genresLock = true,
        tagsLock = true
      )
    }

    seriesMetadataDao.update(updated)
    val modified = seriesMetadataDao.findById(updated.seriesId)

    assertThat(modified.seriesId).isEqualTo(series.id)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(modified.createdDate)
    assertThat(modified.title).isEqualTo(updated.title)
    assertThat(modified.titleSort).isEqualTo(updated.titleSort)
    assertThat(modified.summary).isEqualTo(updated.summary)
    assertThat(modified.status).isEqualTo(updated.status)
    assertThat(modified.readingDirection).isEqualTo(updated.readingDirection)
    assertThat(modified.publisher).isEqualTo(updated.publisher)
    assertThat(modified.ageRating).isEqualTo(updated.ageRating)
    assertThat(modified.language).isEqualTo(updated.language)
    assertThat(modified.genres).containsAll(updated.genres)
    assertThat(modified.tags).containsAll(updated.tags)

    assertThat(modified.titleLock).isTrue()
    assertThat(modified.titleSortLock).isTrue()
    assertThat(modified.statusLock).isTrue()
    assertThat(modified.summaryLock).isTrue()
    assertThat(modified.readingDirectionLock).isTrue()
    assertThat(modified.ageRatingLock).isTrue()
    assertThat(modified.languageLock).isTrue()
    assertThat(modified.genresLock).isTrue()
    assertThat(modified.publisherLock).isTrue()
    assertThat(modified.tagsLock).isTrue()
  }
}