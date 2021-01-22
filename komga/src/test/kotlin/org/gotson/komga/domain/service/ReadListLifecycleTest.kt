package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.language.toIndexedMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ReadListLifecycleTest(
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle

) {

  private val library = makeLibrary("Library1", id = "1")
  private val series = makeSeries("Series1", library.id)

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    seriesLifecycle.createSeries(series)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @Test
  fun `given existing read list when updating bookIds in read list then books are replaced`() {
    val book1 = makeBook("Book1", libraryId = library.id, seriesId = series.id)
    val book2 = makeBook("Book2", libraryId = library.id, seriesId = series.id)
    seriesLifecycle.addBooks(series, listOf(book1, book2))

    val readList = ReadList(name = "readList", bookIds = listOf(book1).map { it.id }.toIndexedMap())
    readListRepository.insert(readList)

    readListLifecycle.updateReadList(readList.copy(bookIds = listOf(book2).map { it.id }.toIndexedMap()))

    val result = readListRepository.findByIdOrNull(readList.id)

    assertThat(result).isNotNull
    assertThat(result!!.bookIds.values).containsExactly(book2.id)
  }

  @Test
  fun `given existing read list with soft deleted book when read list updated and book is restored then that book is in read list with restored order`() {
    //given
    val book1 = makeBook("Book1", libraryId = library.id, seriesId = series.id)
    val book2 = makeBook("Book2", libraryId = library.id, seriesId = series.id)
    val book3 = makeBook("Book3", libraryId = library.id, seriesId = series.id)
    seriesLifecycle.addBooks(series, listOf(book1, book2, book3))

    val readList = ReadList(name = "readList", bookIds = listOf(book1, book2, book3).map { it.id }.toIndexedMap())
    readListRepository.insert(readList)
    bookRepository.update(book2.copy(deleted = true))

    //when
    readListLifecycle.updateReadList(readList.copy(bookIds = listOf(book1, book3).map { it.id }.toIndexedMap()))
    bookRepository.update(book2.copy(deleted = false))

    //then
    val book2Restored = readListRepository.findByIdOrNull(readList.id)!!
    assertThat(book2Restored.bookIds.map { it.key to it.value }).containsExactly(0 to book1.id, 1 to book2.id, 2 to book3.id)
  }
}
