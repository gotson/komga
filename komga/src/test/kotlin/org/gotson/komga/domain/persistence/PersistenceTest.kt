package org.gotson.komga.domain.persistence

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeSerie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional
class PersistenceTest(
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val bookMetadataRepository: BookMetadataRepository,
    @Autowired private val entityManager: TestEntityManager
) {

  @AfterEach
  fun `clear repository`() {
    entityManager.clear()
  }

  @Test
  fun `given serie with book when saving then metadata is also saved`() {
    // given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))

    // when
    serieRepository.save(serie)

    // then
    assertThat(serieRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(1)
    assertThat(bookMetadataRepository.count()).isEqualTo(1)
  }

  @Test
  fun `given existing book when updating metadata then new metadata is saved`() {
    // given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))
    serieRepository.save(serie)

    // when
    val book = bookRepository.findAll().first()
    book.metadata = BookMetadata(status = Status.READY, mediaType = "test", pages = mutableListOf(makeBookPage("page1")))

    bookRepository.save(book)

    // then
    assertThat(serieRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(1)
    assertThat(bookMetadataRepository.count()).isEqualTo(1)
    bookMetadataRepository.findAll().first().let {
      assertThat(it.status == Status.READY)
      assertThat(it.mediaType == "test")
      assertThat(it.pages).hasSize(1)
      assertThat(it.pages.first().fileName).isEqualTo("page1")
    }
  }
}