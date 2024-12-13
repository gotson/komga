package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.offset
import org.gotson.komga.language.toIndexedMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@SpringBootTest
class ReadListDaoTest(
  @Autowired private val readListDao: ReadListDao,
  @Autowired private val bookRepository: BookRepository,
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
    readListDao.deleteAll()
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given read list with books when inserting then it is persisted`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList =
      ReadList(
        name = "MyReadList",
        summary = "summary",
        bookIds = books.map { it.id }.toIndexedMap(),
      )

    // when
    val now = LocalDateTime.now()

    readListDao.insert(readList)
    val created = readListDao.findByIdOrNull(readList.id)!!

    // then
    assertThat(created.name).isEqualTo(readList.name)
    assertThat(created.summary).isEqualTo(readList.summary)
    assertThat(created.createdDate)
      .isEqualTo(created.lastModifiedDate)
      .isCloseTo(now, offset)
    assertThat(created.bookIds.values).containsExactlyElementsOf(books.map { it.id })
  }

  @Test
  fun `given read list with updated books when updating then it is persisted`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList =
      ReadList(
        name = "MyReadList",
        bookIds = books.map { it.id }.toIndexedMap(),
      )

    readListDao.insert(readList)

    // when
    val updatedReadList =
      readList.copy(
        name = "UpdatedReadList",
        summary = "summary",
        bookIds =
          readList.bookIds.values
            .take(5)
            .toIndexedMap(),
      )

    val now = LocalDateTime.now()
    readListDao.update(updatedReadList)
    val updated = readListDao.findByIdOrNull(updatedReadList.id)!!

    // then
    assertThat(updated.name).isEqualTo(updatedReadList.name)
    assertThat(updated.summary).isEqualTo(updatedReadList.summary)
    assertThat(updated.createdDate).isNotEqualTo(updated.lastModifiedDate)
    assertThat(updated.lastModifiedDate).isCloseTo(now, offset)
    assertThat(updated.bookIds.values as Iterable<String>)
      .hasSize(5)
      .containsExactlyElementsOf(books.map { it.id }.take(5))
  }

  @Test
  fun `given read lists with books when removing one book from all then it is removed from all`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList1 =
      ReadList(
        name = "MyReadList",
        bookIds = books.map { it.id }.toIndexedMap(),
      )
    readListDao.insert(readList1)

    val readList2 =
      ReadList(
        name = "MyReadList",
        bookIds = books.map { it.id }.take(5).toIndexedMap(),
      )
    readListDao.insert(readList2)

    // when
    readListDao.removeBookFromAll(books.first().id)

    // then
    val rl1 = readListDao.findByIdOrNull(readList1.id)!!
    assertThat(rl1.bookIds.values as Iterable<String>)
      .hasSize(9)
      .doesNotContain(books.first().id)

    val col2 = readListDao.findByIdOrNull(readList2.id)!!
    assertThat(col2.bookIds.values as Iterable<String>)
      .hasSize(4)
      .doesNotContain(books.first().id)
  }

  @Test
  fun `given read lists spanning different libraries when finding by library then only matching collections are returned`() {
    // given
    val seriesLibrary1 = makeSeries("Series1", library.id).also { seriesRepository.insert(it) }
    val bookLibrary1 = makeBook("Book1", libraryId = library.id, seriesId = seriesLibrary1.id).also { bookRepository.insert(it) }
    val seriesLibrary2 = makeSeries("Series2", library2.id).also { seriesRepository.insert(it) }
    val bookLibrary2 = makeBook("Book2", libraryId = library2.id, seriesId = seriesLibrary2.id).also { bookRepository.insert(it) }

    readListDao.insert(
      ReadList(
        name = "readListLibrary1",
        bookIds = listOf(bookLibrary1.id).toIndexedMap(),
      ),
    )

    readListDao.insert(
      ReadList(
        name = "readListLibrary2",
        bookIds = listOf(bookLibrary2.id).toIndexedMap(),
      ),
    )

    readListDao.insert(
      ReadList(
        name = "readListLibraryBoth",
        bookIds = listOf(bookLibrary1.id, bookLibrary2.id).toIndexedMap(),
      ),
    )

    // when
    val foundLibrary1Filtered = readListDao.findAll(listOf(library.id), listOf(library.id), pageable = Pageable.unpaged()).content
    val foundLibrary1Unfiltered = readListDao.findAll(listOf(library.id), null, pageable = Pageable.unpaged()).content
    val foundLibrary2Filtered = readListDao.findAll(listOf(library2.id), listOf(library2.id), pageable = Pageable.unpaged()).content
    val foundLibrary2Unfiltered = readListDao.findAll(listOf(library2.id), null, pageable = Pageable.unpaged()).content
    val foundBothUnfiltered = readListDao.findAll(listOf(library.id, library2.id), null, pageable = Pageable.unpaged()).content

    // then
    assertThat(foundLibrary1Filtered).hasSize(2)
    assertThat(foundLibrary1Filtered.map { it.name }).containsExactly("readListLibrary1", "readListLibraryBoth")
    with(foundLibrary1Filtered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values as Iterable<String>)
        .hasSize(1)
        .containsExactly(bookLibrary1.id)
      assertThat(filtered).isTrue
    }

    assertThat(foundLibrary1Unfiltered).hasSize(2)
    assertThat(foundLibrary1Unfiltered.map { it.name }).containsExactly("readListLibrary1", "readListLibraryBoth")
    with(foundLibrary1Unfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values as Iterable<String>)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse
    }

    assertThat(foundLibrary2Filtered).hasSize(2)
    assertThat(foundLibrary2Filtered.map { it.name }).containsExactly("readListLibrary2", "readListLibraryBoth")
    with(foundLibrary2Filtered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values as Iterable<String>)
        .hasSize(1)
        .containsExactly(bookLibrary2.id)
      assertThat(filtered).isTrue
    }

    assertThat(foundLibrary2Unfiltered).hasSize(2)
    assertThat(foundLibrary2Unfiltered.map { it.name }).containsExactly("readListLibrary2", "readListLibraryBoth")
    with(foundLibrary2Unfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values as Iterable<String>)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse
    }

    assertThat(foundBothUnfiltered).hasSize(3)
    assertThat(foundBothUnfiltered.map { it.name }).containsExactly("readListLibrary1", "readListLibrary2", "readListLibraryBoth")
    with(foundBothUnfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values as Iterable<String>)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse
    }
  }
}
