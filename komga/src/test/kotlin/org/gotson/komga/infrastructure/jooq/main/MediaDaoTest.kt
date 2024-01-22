package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.ProxyExtension
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.jooq.offset
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class MediaDaoTest(
  @Autowired private val mediaDao: MediaDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val komgaProperties: KomgaProperties,
) {
  private val library = makeLibrary()
  private val series = makeSeries("Series", libraryId = library.id)
  private val book = makeBook("Book", libraryId = library.id, seriesId = series.id)

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)

    seriesRepository.insert(series)

    bookRepository.insert(book)
  }

  @AfterEach
  fun deleteMedia() {
    bookRepository.findAll().forEach {
      mediaDao.delete(it.id)
    }
  }

  @AfterAll
  fun tearDown() {
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a media when inserting then it is persisted`() {
    val now = LocalDateTime.now()
    val media =
      Media(
        status = Media.Status.READY,
        mediaType = "application/zip",
        pages =
          listOf(
            BookPage(
              fileName = "1.jpg",
              mediaType = "image/jpeg",
              dimension = Dimension(10, 10),
              fileHash = "hashed",
              fileSize = 10,
            ),
          ),
        files = listOf(MediaFile("ComicInfo.xml", "application/xml", MediaFile.SubType.EPUB_ASSET, 3)),
        comment = "comment",
        bookId = book.id,
      )

    mediaDao.insert(media)
    val created = mediaDao.findById(media.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)
    assertThat(created.status).isEqualTo(media.status)
    assertThat(created.mediaType).isEqualTo(media.mediaType)
    assertThat(created.comment).isEqualTo(media.comment)
    assertThat(created.pages).hasSize(1)
    with(created.pages.first()) {
      assertThat(fileName).isEqualTo(media.pages.first().fileName)
      assertThat(mediaType).isEqualTo(media.pages.first().mediaType)
      assertThat(dimension).isEqualTo(media.pages.first().dimension)
      assertThat(fileHash).isEqualTo(media.pages.first().fileHash)
      assertThat(fileSize).isEqualTo(media.pages.first().fileSize)
    }
    assertThat(created.files).hasSize(1)
    with(created.files.first()) {
      assertThat(fileName).isEqualTo(media.files.first().fileName)
      assertThat(mediaType).isEqualTo(media.files.first().mediaType)
      assertThat(subType).isEqualTo(media.files.first().subType)
      assertThat(fileSize).isEqualTo(media.files.first().fileSize)
    }
  }

  @Test
  fun `given a minimum media when inserting then it is persisted`() {
    val media = Media(bookId = book.id)

    mediaDao.insert(media)
    val created = mediaDao.findById(media.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.status).isEqualTo(Media.Status.UNKNOWN)
    assertThat(created.mediaType).isNull()
    assertThat(created.comment).isNull()
    assertThat(created.pages).isEmpty()
    assertThat(created.files).isEmpty()
    assertThat(created.extension).isNull()
  }

  @Test
  fun `given existing media when updating then it is persisted`() {
    val media =
      Media(
        status = Media.Status.READY,
        mediaType = "application/zip",
        pages =
          listOf(
            BookPage(
              fileName = "1.jpg",
              mediaType = "image/jpeg",
            ),
          ),
        files = listOf(MediaFile("ComicInfo.xml", "application/xml", MediaFile.SubType.EPUB_ASSET, 5)),
        comment = "comment",
        bookId = book.id,
      )
    mediaDao.insert(media)

    val modificationDate = LocalDateTime.now()

    val updated =
      with(mediaDao.findById(media.bookId)) {
        copy(
          status = Media.Status.ERROR,
          mediaType = "application/rar",
          pages =
            listOf(
              BookPage(
                fileName = "2.png",
                mediaType = "image/png",
                dimension = Dimension(10, 10),
                fileHash = "hashed",
                fileSize = 10,
              ),
            ),
          files = listOf(MediaFile("id.txt")),
          comment = "comment2",
        )
      }

    mediaDao.update(updated)
    val modified = mediaDao.findById(updated.bookId)

    assertThat(modified.bookId).isEqualTo(updated.bookId)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(updated.lastModifiedDate)
    assertThat(modified.status).isEqualTo(updated.status)
    assertThat(modified.mediaType).isEqualTo(updated.mediaType)
    assertThat(modified.comment).isEqualTo(updated.comment)
    assertThat(modified.pages.first().fileName).isEqualTo(updated.pages.first().fileName)
    assertThat(modified.pages.first().mediaType).isEqualTo(updated.pages.first().mediaType)
    assertThat(modified.pages.first().dimension).isEqualTo(updated.pages.first().dimension)
    assertThat(modified.pages.first().fileHash).isEqualTo(updated.pages.first().fileHash)
    assertThat(modified.pages.first().fileSize).isEqualTo(updated.pages.first().fileSize)
    assertThat(modified.files.first().fileName).isEqualTo(updated.files.first().fileName)
    assertThat(modified.files.first().mediaType).isEqualTo(updated.files.first().mediaType)
    assertThat(modified.files.first().subType).isEqualTo(updated.files.first().subType)
    assertThat(modified.files.first().fileSize).isEqualTo(updated.files.first().fileSize)
  }

  @Test
  fun `given existing media when finding by id then media is returned`() {
    val media =
      Media(
        status = Media.Status.READY,
        mediaType = "application/zip",
        pages =
          listOf(
            BookPage(
              fileName = "1.jpg",
              mediaType = "image/jpeg",
            ),
          ),
        files = listOf(MediaFile("ComicInfo.xml")),
        comment = "comment",
        bookId = book.id,
      )
    mediaDao.insert(media)

    val found = catchThrowable { mediaDao.findById(media.bookId) }

    assertThat(found).doesNotThrowAnyException()
  }

  @Test
  fun `given non-existing media when finding by id then exception is thrown`() {
    val found = catchThrowable { mediaDao.findById("128742") }

    assertThat(found).isInstanceOf(Exception::class.java)
  }

  @Nested
  inner class MediaExtension {
    @Test
    fun `given a media with extension when inserting then it is persisted`() {
      val media =
        Media(
          status = Media.Status.READY,
          mediaType = "application/epub+zip",
          extension =
            MediaExtensionEpub(
              toc = listOf(EpubTocEntry("title", "href", listOf(EpubTocEntry("subtitle", "subhref")))),
              landmarks = listOf(EpubTocEntry("title2", "href2", listOf(EpubTocEntry("subtitle2", "subhref2")))),
            ),
          bookId = book.id,
        )

      mediaDao.insert(media)
      val created = mediaDao.findById(media.bookId)

      assertThat(created.extension).isNotNull
      assertThat(created.extension).isInstanceOf(ProxyExtension::class.java)
      assertThat((created.extension as ProxyExtension).extensionClassName).isEqualTo(MediaExtensionEpub::class.qualifiedName)

      val extension = mediaDao.findExtensionByIdOrNull(media.bookId)
      assertThat(extension).isNotNull
      assertThat(extension).isInstanceOf(MediaExtensionEpub::class.java)
      assertThat(extension).isEqualTo(media.extension)
    }

    @Test
    fun `given existing media with extension when updating then it is persisted`() {
      val media =
        Media(
          status = Media.Status.READY,
          mediaType = "application/epub+zip",
          extension =
            MediaExtensionEpub(
              landmarks = listOf(EpubTocEntry("title2", "href2", listOf(EpubTocEntry("subtitle2", "subhref2")))),
            ),
          bookId = book.id,
        )
      mediaDao.insert(media)

      val updated =
        with(mediaDao.findById(media.bookId)) {
          copy(
            extension =
              MediaExtensionEpub(
                toc = listOf(EpubTocEntry("title", "href", listOf(EpubTocEntry("subtitle", "subhref")))),
              ),
          )
        }

      mediaDao.update(updated)
      val modified = mediaDao.findById(updated.bookId)

      assertThat(modified.bookId).isEqualTo(updated.bookId)
      assertThat(modified.createdDate).isEqualTo(updated.createdDate)
      assertThat(modified.lastModifiedDate).isNotEqualTo(updated.lastModifiedDate)
      assertThat(modified.extension).isNotNull
      assertThat(modified.extension).isInstanceOf(ProxyExtension::class.java)
      assertThat((modified.extension as ProxyExtension).extensionClassName).isEqualTo(MediaExtensionEpub::class.qualifiedName)

      assertThat(mediaDao.findExtensionByIdOrNull(media.bookId)).isEqualTo(updated.extension)
    }

    @Test
    fun `given existing media with proxy extension when updating then it is kept as-is`() {
      val media =
        Media(
          status = Media.Status.READY,
          mediaType = "application/epub+zip",
          extension =
            MediaExtensionEpub(
              landmarks = listOf(EpubTocEntry("title2", "href2", listOf(EpubTocEntry("subtitle2", "subhref2")))),
            ),
          bookId = book.id,
        )
      mediaDao.insert(media)

      val updated = mediaDao.findById(media.bookId).copy(comment = "updated")

      mediaDao.update(updated)
      val modified = mediaDao.findById(updated.bookId)

      assertThat(modified.bookId).isEqualTo(updated.bookId)
      assertThat(modified.createdDate).isEqualTo(updated.createdDate)
      assertThat(modified.lastModifiedDate).isNotEqualTo(updated.lastModifiedDate)
      assertThat(modified.comment).isEqualTo(updated.comment)
      assertThat(modified.extension).isNotNull
      assertThat(modified.extension).isInstanceOf(ProxyExtension::class.java)
      assertThat((modified.extension as ProxyExtension).extensionClassName).isEqualTo(MediaExtensionEpub::class.qualifiedName)

      val extension = mediaDao.findExtensionByIdOrNull(media.bookId)
      assertThat(extension).isNotNull
      assertThat(extension).isInstanceOf(MediaExtensionEpub::class.java)
      assertThat(extension).isEqualTo(media.extension)
    }
  }

  @Nested
  inner class MissingPageHash {
    @Test
    fun `given media with single page not hashed when finding for missing page hash then it is returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            listOf(
              BookPage(
                fileName = "1.jpg",
                mediaType = "image/jpeg",
              ),
            ),
          mediaType = MediaType.ZIP.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).containsExactly(book.id)
    }

    @Test
    fun `given non-convertible media not hashed when finding for missing page hash then it is returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            listOf(
              BookPage(
                fileName = "1.jpg",
                mediaType = "image/jpeg",
              ),
            ),
          mediaType = MediaType.RAR_4.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with no pages hashed when finding for missing page hash then it is not returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            (1..12).map {
              BookPage(
                fileName = "$it.jpg",
                mediaType = "image/jpeg",
              )
            },
          mediaType = MediaType.ZIP.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).containsOnly(book.id)
    }

    @Test
    fun `given media with single page hashed when finding for missing page hash then it is not returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            listOf(
              BookPage(
                fileName = "1.jpg",
                mediaType = "image/jpeg",
                fileHash = "hashed",
              ),
            ),
          mediaType = MediaType.ZIP.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with required pages hashed when finding for missing page hash then it is not returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            (1..12).map {
              BookPage(
                fileName = "$it.jpg",
                mediaType = "image/jpeg",
                fileHash = if (it <= 3 || it >= 9) "hashed" else "",
              )
            },
          mediaType = MediaType.ZIP.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with more pages hashed than required when finding for missing page hash then it is not returned`() {
      val media =
        Media(
          status = Media.Status.READY,
          pages =
            (1..12).map {
              BookPage(
                fileName = "$it.jpg",
                mediaType = "image/jpeg",
                fileHash = "hashed",
              )
            },
          mediaType = MediaType.ZIP.type,
          bookId = book.id,
        )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.type), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }
  }
}
