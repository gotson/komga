package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Library
import org.junit.jupiter.api.AfterEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class LibraryDaoTest(
  @Autowired private val libraryDao: LibraryDao
) {

  @AfterEach
  fun deleteLibraries() {
    libraryDao.deleteAll()
    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `given a library when inserting then it is persisted`() {
    val now = LocalDateTime.now()
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )

    Thread.sleep(5)

    val created = libraryDao.insert(library)

    assertThat(created.id).isNotEqualTo(0)
    assertThat(created.createdDate).isAfter(now)
    assertThat(created.lastModifiedDate).isAfter(now)
    assertThat(created.name).isEqualTo(library.name)
    assertThat(created.root).isEqualTo(library.root)
  }

  @Test
  fun `given a library when deleting then it is deleted`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )

    val created = libraryDao.insert(library)
    assertThat(libraryDao.count()).isEqualTo(1)

    libraryDao.delete(created.id)

    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `given libraries when deleting all then all are deleted`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )
    val library2 = Library(
      name = "Library2",
      root = URL("file://library2")
    )

    libraryDao.insert(library)
    libraryDao.insert(library2)
    assertThat(libraryDao.count()).isEqualTo(2)

    libraryDao.deleteAll()

    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `given libraries when finding all then all are returned`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )
    val library2 = Library(
      name = "Library2",
      root = URL("file://library2")
    )

    libraryDao.insert(library)
    libraryDao.insert(library2)

    val all = libraryDao.findAll()

    assertThat(all).hasSize(2)
    assertThat(all.map { it.name }).containsExactlyInAnyOrder("Library", "Library2")
  }

  @Test
  fun `given libraries when finding all by id then all are returned`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )
    val library2 = Library(
      name = "Library2",
      root = URL("file://library2")
    )

    val created1 = libraryDao.insert(library)
    val created2 = libraryDao.insert(library2)

    val all = libraryDao.findAllById(listOf(created1.id, created2.id))

    assertThat(all).hasSize(2)
    assertThat(all.map { it.name }).containsExactlyInAnyOrder("Library", "Library2")
  }

  @Test
  fun `given existing library when finding by id then library is returned`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )

    val created = libraryDao.insert(library)

    val found = libraryDao.findByIdOrNull(created.id)

    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Library")
  }

  @Test
  fun `given non-existing library when finding by id then null is returned`() {
    val found = libraryDao.findByIdOrNull(1287386)

    assertThat(found).isNull()
  }

  @Test
  fun `given libraries when checking if exists by name then returns true or false`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )
    libraryDao.insert(library)

    val exists = libraryDao.existsByName("LIBRARY")
    val notExists = libraryDao.existsByName("LIBRARY2")

    assertThat(exists).isTrue()
    assertThat(notExists).isFalse()
  }
}
