package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.offset
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@SpringBootTest
class SeriesCollectionDaoTest(
  @Autowired private val collectionDao: SeriesCollectionDao,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val library = makeLibrary()
  private val library2 = makeLibrary("library2")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    libraryRepository.insert(library2)
  }

  @AfterEach
  fun deleteSeries() {
    collectionDao.deleteAll()
    seriesRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given collection with series when inserting then it is persisted`() {
    // given
    val series = (1..10).map { makeSeries("Series $it", library.id) }
    series.forEach { seriesRepository.insert(it) }

    val collection =
      SeriesCollection(
        name = "MyCollection",
        seriesIds = series.map { it.id },
      )

    // when
    val now = LocalDateTime.now()

    collectionDao.insert(collection)
    val created = collectionDao.findByIdOrNull(collection.id)!!

    // then
    assertThat(created.name).isEqualTo(collection.name)
    assertThat(created.ordered).isEqualTo(collection.ordered)
    assertThat(created.createdDate)
      .isEqualTo(created.lastModifiedDate)
      .isCloseTo(now, offset)
    assertThat(created.seriesIds).containsExactlyElementsOf(series.map { it.id })
  }

  @Test
  fun `given collection with updated series when updating then it is persisted`() {
    // given
    val series = (1..10).map { makeSeries("Series $it", library.id) }
    series.forEach { seriesRepository.insert(it) }

    val collection =
      SeriesCollection(
        name = "MyCollection",
        seriesIds = series.map { it.id },
      )

    collectionDao.insert(collection)

    // when
    val updatedCollection =
      collection.copy(
        name = "UpdatedCollection",
        ordered = true,
        seriesIds = collection.seriesIds.take(5),
      )

    val now = LocalDateTime.now()
    collectionDao.update(updatedCollection)
    val updated = collectionDao.findByIdOrNull(updatedCollection.id)!!

    // then
    assertThat(updated.name).isEqualTo(updatedCollection.name)
    assertThat(updated.ordered).isEqualTo(updatedCollection.ordered)
    assertThat(updated.createdDate).isNotEqualTo(updated.lastModifiedDate)
    assertThat(updated.lastModifiedDate).isCloseTo(now, offset)
    assertThat(updated.seriesIds)
      .hasSize(5)
      .containsExactlyElementsOf(series.map { it.id }.take(5))
  }

  @Test
  fun `given collections with series when removing one series from all then it is removed from all`() {
    // given
    val series = (1..10).map { makeSeries("Series $it", library.id) }
    series.forEach { seriesRepository.insert(it) }

    val collection1 =
      SeriesCollection(
        name = "MyCollection",
        seriesIds = series.map { it.id },
      )
    collectionDao.insert(collection1)

    val collection2 =
      SeriesCollection(
        name = "MyCollection2",
        seriesIds = series.map { it.id }.take(5),
      )
    collectionDao.insert(collection2)

    // when
    collectionDao.removeSeriesFromAll(series.first().id)

    // then
    val col1 = collectionDao.findByIdOrNull(collection1.id)!!
    assertThat(col1.seriesIds)
      .hasSize(9)
      .doesNotContain(series.first().id)

    val col2 = collectionDao.findByIdOrNull(collection2.id)!!
    assertThat(col2.seriesIds)
      .hasSize(4)
      .doesNotContain(series.first().id)
  }

  @Test
  fun `given collections spanning different libraries when finding by library then only matching collections are returned`() {
    // given
    val seriesLibrary1 = makeSeries("Series1", library.id).also { seriesRepository.insert(it) }
    val seriesLibrary2 = makeSeries("Series2", library2.id).also { seriesRepository.insert(it) }

    collectionDao.insert(
      SeriesCollection(
        name = "collectionLibrary1",
        seriesIds = listOf(seriesLibrary1.id),
      ),
    )

    collectionDao.insert(
      SeriesCollection(
        name = "collectionLibrary2",
        seriesIds = listOf(seriesLibrary2.id),
      ),
    )

    collectionDao.insert(
      SeriesCollection(
        name = "collectionLibraryBoth",
        seriesIds = listOf(seriesLibrary1.id, seriesLibrary2.id),
      ),
    )

    // when
    val foundLibrary1Filtered = collectionDao.findAll(listOf(library.id), listOf(library.id), pageable = Pageable.unpaged()).content
    val foundLibrary1Unfiltered = collectionDao.findAll(listOf(library.id), null, pageable = Pageable.unpaged()).content
    val foundLibrary2Filtered = collectionDao.findAll(listOf(library2.id), listOf(library2.id), pageable = Pageable.unpaged()).content
    val foundLibrary2Unfiltered = collectionDao.findAll(listOf(library2.id), null, pageable = Pageable.unpaged()).content
    val foundBothUnfiltered = collectionDao.findAll(listOf(library.id, library2.id), null, pageable = Pageable.unpaged()).content

    // then
    assertThat(foundLibrary1Filtered).hasSize(2)
    assertThat(foundLibrary1Filtered.map { it.name }).containsExactly("collectionLibrary1", "collectionLibraryBoth")
    with(foundLibrary1Filtered.find { it.name == "collectionLibraryBoth" }!!) {
      assertThat(seriesIds)
        .hasSize(1)
        .containsExactly(seriesLibrary1.id)
      assertThat(filtered).isTrue
    }

    assertThat(foundLibrary1Unfiltered).hasSize(2)
    assertThat(foundLibrary1Unfiltered.map { it.name }).containsExactly("collectionLibrary1", "collectionLibraryBoth")
    with(foundLibrary1Unfiltered.find { it.name == "collectionLibraryBoth" }!!) {
      assertThat(seriesIds)
        .hasSize(2)
        .containsExactly(seriesLibrary1.id, seriesLibrary2.id)
      assertThat(filtered).isFalse
    }

    assertThat(foundLibrary2Filtered).hasSize(2)
    assertThat(foundLibrary2Filtered.map { it.name }).containsExactly("collectionLibrary2", "collectionLibraryBoth")
    with(foundLibrary2Filtered.find { it.name == "collectionLibraryBoth" }!!) {
      assertThat(seriesIds)
        .hasSize(1)
        .containsExactly(seriesLibrary2.id)
      assertThat(filtered).isTrue
    }

    assertThat(foundLibrary2Unfiltered).hasSize(2)
    assertThat(foundLibrary2Unfiltered.map { it.name }).containsExactly("collectionLibrary2", "collectionLibraryBoth")
    with(foundLibrary2Unfiltered.find { it.name == "collectionLibraryBoth" }!!) {
      assertThat(seriesIds)
        .hasSize(2)
        .containsExactly(seriesLibrary1.id, seriesLibrary2.id)
      assertThat(filtered).isFalse
    }

    assertThat(foundBothUnfiltered).hasSize(3)
    assertThat(foundBothUnfiltered.map { it.name }).containsExactly("collectionLibrary1", "collectionLibrary2", "collectionLibraryBoth")
    with(foundBothUnfiltered.find { it.name == "collectionLibraryBoth" }!!) {
      assertThat(seriesIds)
        .hasSize(2)
        .containsExactly(seriesLibrary1.id, seriesLibrary2.id)
      assertThat(filtered).isFalse
    }
  }
}
