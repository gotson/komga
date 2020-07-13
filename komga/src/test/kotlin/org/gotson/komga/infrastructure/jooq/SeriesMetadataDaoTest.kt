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
      seriesId = series.id
    )

    val created = seriesMetadataDao.insert(metadata)

    assertThat(created.seriesId).isEqualTo(series.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)
    assertThat(created.title).isEqualTo("Series")
    assertThat(created.titleSort).isEqualTo("Series, The")
    assertThat(created.status).isEqualTo(SeriesMetadata.Status.ENDED)
    assertThat(created.titleLock).isFalse()
    assertThat(created.titleSortLock).isFalse()
    assertThat(created.statusLock).isFalse()
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
      seriesId = series.id
    )
    val created = seriesMetadataDao.insert(metadata)


    val modificationDate = LocalDateTime.now()

    val updated = with(created) {
      copy(
        status = SeriesMetadata.Status.HIATUS,
        title = "Changed",
        titleSort = "Changed, The",
        statusLock = true,
        titleLock = true,
        titleSortLock = true
      )
    }

    seriesMetadataDao.update(updated)
    val modified = seriesMetadataDao.findById(updated.seriesId)

    assertThat(modified.seriesId).isEqualTo(series.id)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(modified.createdDate)
    assertThat(modified.title).isEqualTo("Changed")
    assertThat(modified.titleSort).isEqualTo("Changed, The")
    assertThat(modified.status).isEqualTo(SeriesMetadata.Status.HIATUS)
    assertThat(modified.titleLock).isTrue()
    assertThat(modified.titleSortLock).isTrue()
    assertThat(modified.statusLock).isTrue()
  }
}
