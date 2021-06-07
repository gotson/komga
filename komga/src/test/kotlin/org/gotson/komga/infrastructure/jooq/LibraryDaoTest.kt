package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Library
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
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

    libraryDao.insert(library)
    val created = libraryDao.findById(library.id)

    assertThat(created.id).isNotEqualTo(0)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)
    assertThat(created.name).isEqualTo(library.name)
    assertThat(created.root).isEqualTo(library.root)
  }

  @Test
  fun `given existing library when updating then it is persisted`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )
    libraryDao.insert(library)

    val modificationDate = LocalDateTime.now()

    val updated = with(libraryDao.findById(library.id)) {
      copy(
        name = "LibraryUpdated",
        root = URL("file://library2"),
        importEpubSeries = false,
        importEpubBook = false,
        importComicInfoCollection = false,
        importComicInfoSeries = false,
        importComicInfoBook = false,
        importComicInfoReadList = false,
        importMylarSeries = false,
        importBarcodeIsbn = false,
        importLocalArtwork = false,
        repairExtensions = true,
        convertToCbz = true,
      )
    }

    libraryDao.update(updated)
    val modified = libraryDao.findById(updated.id)

    assertThat(modified.id).isEqualTo(updated.id)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(updated.lastModifiedDate)

    assertThat(modified.name).isEqualTo(updated.name)
    assertThat(modified.root).isEqualTo(updated.root)
    assertThat(modified.importEpubSeries).isEqualTo(updated.importEpubSeries)
    assertThat(modified.importEpubBook).isEqualTo(updated.importEpubBook)
    assertThat(modified.importComicInfoCollection).isEqualTo(updated.importComicInfoCollection)
    assertThat(modified.importComicInfoSeries).isEqualTo(updated.importComicInfoSeries)
    assertThat(modified.importComicInfoBook).isEqualTo(updated.importComicInfoBook)
    assertThat(modified.importComicInfoReadList).isEqualTo(updated.importComicInfoReadList)
    assertThat(modified.importBarcodeIsbn).isEqualTo(updated.importBarcodeIsbn)
    assertThat(modified.importLocalArtwork).isEqualTo(updated.importLocalArtwork)
    assertThat(modified.importMylarSeries).isEqualTo(updated.importMylarSeries)
    assertThat(modified.repairExtensions).isEqualTo(updated.repairExtensions)
    assertThat(modified.convertToCbz).isEqualTo(updated.convertToCbz)
  }

  @Test
  fun `given a library when deleting then it is deleted`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )

    libraryDao.insert(library)
    assertThat(libraryDao.count()).isEqualTo(1)

    libraryDao.delete(library.id)

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

    libraryDao.insert(library)
    libraryDao.insert(library2)

    val all = libraryDao.findAllByIds(listOf(library.id, library2.id))

    assertThat(all).hasSize(2)
    assertThat(all.map { it.name }).containsExactlyInAnyOrder("Library", "Library2")
  }

  @Test
  fun `given existing library when finding by id then library is returned`() {
    val library = Library(
      name = "Library",
      root = URL("file://library")
    )

    libraryDao.insert(library)

    val found = libraryDao.findByIdOrNull(library.id)

    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Library")
  }

  @Test
  fun `given non-existing library when finding by id then null is returned`() {
    val found = libraryDao.findByIdOrNull("1287386")

    assertThat(found).isNull()
  }
}
