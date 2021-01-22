package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class SeriesCollectionLifecycleTest(
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val collectionLifecycle: SeriesCollectionLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle
) {

  private val library = makeLibrary("Library1", id = "1")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @Test
  fun `given existing collection when updating seriesIds in collection then series are replaced`() {
    val series1 = makeSeries("Series1", libraryId = library.id)
    val series2 = makeSeries("Series2", libraryId = library.id)
    seriesLifecycle.createSeries(series1)
    seriesLifecycle.createSeries(series2)

    val collection = SeriesCollection(name = "collection1", seriesIds = listOf(series1, series2).map { it.id }, ordered = true)
    collectionRepository.insert(collection)
    collectionLifecycle.updateCollection(collection.copy(seriesIds = listOf(series2).map { it.id }))

    val result = collectionRepository.findByIdOrNull(collection.id)

    assertThat(result).isNotNull
    assertThat(result!!.seriesIds).containsExactly(series2.id)
  }

  @Test
  fun `given existing collection with soft deleted series when collection is updated and series is restored then that series is in collection with restored order`() {
    //given
    val series1 = makeSeries("Series1", libraryId = library.id)
    val series2 = makeSeries("Series2", libraryId = library.id)
    val series3 = makeSeries("Series3", libraryId = library.id)
    seriesLifecycle.createSeries(series1)
    seriesLifecycle.createSeries(series2)
    seriesLifecycle.createSeries(series3)

    val collection = SeriesCollection(name = "collection1", seriesIds = listOf(series1, series2, series3).map { it.id }, ordered = true)
    collectionRepository.insert(collection)
    seriesRepository.update(series2.copy(deleted = true))

    //when
    collectionLifecycle.updateCollection(collection.copy(seriesIds = listOf(series1, series3).map { it.id }))
    seriesRepository.update(series2.copy(deleted = false))

    //then
    val series2Restored = collectionRepository.findByIdOrNull(collection.id)!!
    assertThat(series2Restored.seriesIds).containsExactly(series1.id, series2.id, series3.id)
  }
}
