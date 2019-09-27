package org.gotson.komga.domain.persistence

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSerie
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional
class AuditableEntityTest(
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val libraryRepository: LibraryRepository
) {

  private val library = makeLibrary()

  @BeforeAll
  fun `setup library`() {
    libraryRepository.save(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    serieRepository.deleteAll()
  }

  @Test
  fun `given serie with book when saving then created and modified date is also saved`() {
    // given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1"))).also { it.library = library }

    // when
    serieRepository.save(serie)

    // then
    assertThat(serie.createdDate).isBefore(LocalDateTime.now())
    assertThat(serie.lastModifiedDate).isBefore(LocalDateTime.now())
    assertThat(serie.books.first().createdDate).isBefore(LocalDateTime.now())
    assertThat(serie.books.first().lastModifiedDate).isBefore(LocalDateTime.now())
  }

  @Test
  fun `given existing serie with book when updating serie only then created date is kept and modified date is changed for serie only`() {
    // given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1"))).also { it.library = library }

    serieRepository.save(serie)

    val creationTimeApprox = LocalDateTime.now()

    Thread.sleep(1000)

    // when
    serie.name = "serieUpdated"
    serieRepository.saveAndFlush(serie)

    val modificationTimeApprox = LocalDateTime.now()

    // then
    assertThat(serie.createdDate)
        .isBefore(creationTimeApprox)
        .isNotEqualTo(serie.lastModifiedDate)
    assertThat(serie.lastModifiedDate)
        .isAfter(creationTimeApprox)
        .isBefore(modificationTimeApprox)

    assertThat(serie.books.first().createdDate)
        .isBefore(creationTimeApprox)
        .isEqualTo(serie.books.first().lastModifiedDate)
  }

  @Test
  fun `given existing serie with book when updating book only then created date is kept and modified date is changed for book only`() {
    // given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1"))).also { it.library = library }

    serieRepository.save(serie)

    val creationTimeApprox = LocalDateTime.now()

    Thread.sleep(1000)

    // when
    serie.books.first().name = "bookUpdated"
    serieRepository.saveAndFlush(serie)

    val modificationTimeApprox = LocalDateTime.now()

    // then
    assertThat(serie.createdDate)
        .isBefore(creationTimeApprox)
        .isEqualTo(serie.lastModifiedDate)

    assertThat(serie.books.first().createdDate)
        .isBefore(creationTimeApprox)
        .isNotEqualTo(serie.books.first().lastModifiedDate)
    assertThat(serie.books.first().lastModifiedDate)
        .isAfter(creationTimeApprox)
        .isBefore(modificationTimeApprox)
  }
}