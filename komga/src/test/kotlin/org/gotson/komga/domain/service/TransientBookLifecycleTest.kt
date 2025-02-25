package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.TransientBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.metadata.comicrack.ComicInfoProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TransientBookLifecycleTest(
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val transientBookLifecycle: TransientBookLifecycle,
) {
  private val library = makeLibrary()

  @SpykBean
  private lateinit var mockProvider: ComicInfoProvider

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun cleanup() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `when getting metadata for transient book then the most specific series name is matched first`() {
    seriesLifecycle.createSeries(makeSeries("Batman and Robin", libraryId = library.id))
    val seriesExact = makeSeries("Batman", libraryId = library.id)
    seriesLifecycle.createSeries(seriesExact)
    seriesLifecycle.createSeries(makeSeries("Batman and Robin (2022)", libraryId = library.id))

    val book =
      TransientBook(
        makeBook("whatever"),
        Media(),
      )

    every { mockProvider.getBookMetadataFromBook(any()) } returns BookMetadataPatch(null, null, null, 15F, null, null, null, null, null, emptyList())
    every { mockProvider.getSeriesMetadataFromBook(any(), any()) } returns SeriesMetadataPatch("BATMAN", null, null, null, null, null, null, null, null, null, emptySet())

    val (seriesId, number) = transientBookLifecycle.getMetadata(book)

    assertThat(seriesId).isEqualTo(seriesExact.id)
    assertThat(number).isEqualTo(15F)
  }
}
