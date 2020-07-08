package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.LibraryRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class LibraryDao(
  private val dsl: DSLContext
) : LibraryRepository {

  private val l = Tables.LIBRARY
  private val ul = Tables.USER_LIBRARY_SHARING

  override fun findByIdOrNull(libraryId: Long): Library? =
    findOne(libraryId)
      ?.toDomain()

  override fun findById(libraryId: Long): Library =
    findOne(libraryId)
      .toDomain()

  private fun findOne(libraryId: Long) =
    dsl.selectFrom(l)
      .where(l.ID.eq(libraryId))
      .fetchOneInto(l)

  override fun findAll(): Collection<Library> =
    dsl.selectFrom(l)
      .fetchInto(l)
      .map { it.toDomain() }

  override fun findAllById(libraryIds: Collection<Long>): Collection<Library> =
    dsl.selectFrom(l)
      .where(l.ID.`in`(libraryIds))
      .fetchInto(l)
      .map { it.toDomain() }

  override fun delete(libraryId: Long) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).where(ul.LIBRARY_ID.eq(libraryId)).execute()
        deleteFrom(l).where(l.ID.eq(libraryId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).execute()
        deleteFrom(l).execute()
      }
    }
  }

  override fun insert(library: Library): Library {
    val record = dsl.insertInto(l)
      .set(l.NAME, library.name)
      .set(l.ROOT, library.root.toString())
      .set(l.IMPORT_COMICINFO_BOOK, library.importComicInfoBook)
      .set(l.IMPORT_COMICINFO_SERIES, library.importComicInfoSeries)
      .set(l.IMPORT_COMICINFO_COLLECTION, library.importComicInfoCollection)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .returning(l.ID)
      .fetchOne()

    return findById(record.id)
  }

  override fun update(library: Library) {
    dsl.update(l)
      .set(l.NAME, library.name)
      .set(l.ROOT, library.root.toString())
      .set(l.IMPORT_COMICINFO_BOOK, library.importComicInfoBook)
      .set(l.IMPORT_COMICINFO_SERIES, library.importComicInfoSeries)
      .set(l.IMPORT_COMICINFO_COLLECTION, library.importComicInfoCollection)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .set(l.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(l.ID.eq(library.id))
      .execute()
  }

  override fun count(): Long = dsl.fetchCount(l).toLong()


  private fun LibraryRecord.toDomain() =
    Library(
      name = name,
      root = URL(root),
      importComicInfoBook = importComicinfoBook,
      importComicInfoSeries = importComicinfoSeries,
      importComicInfoCollection = importComicinfoCollection,
      importEpubBook = importEpubBook,
      importEpubSeries = importEpubSeries,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone()
    )
}
