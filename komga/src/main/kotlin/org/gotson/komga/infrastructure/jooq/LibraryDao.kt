package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.LibraryRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class LibraryDao(
  private val dsl: DSLContext,
) : LibraryRepository {

  private val l = Tables.LIBRARY
  private val ul = Tables.USER_LIBRARY_SHARING

  override fun findByIdOrNull(libraryId: String): Library? =
    findOne(libraryId)
      ?.toDomain()

  override fun findById(libraryId: String): Library =
    findOne(libraryId)!!
      .toDomain()

  private fun findOne(libraryId: String) =
    dsl.selectFrom(l)
      .where(l.ID.eq(libraryId))
      .fetchOneInto(l)

  override fun findAll(): Collection<Library> =
    dsl.selectFrom(l)
      .fetchInto(l)
      .map { it.toDomain() }

  override fun findAllByIds(libraryIds: Collection<String>): Collection<Library> =
    dsl.selectFrom(l)
      .where(l.ID.`in`(libraryIds))
      .fetchInto(l)
      .map { it.toDomain() }

  @Transactional
  override fun delete(libraryId: String) {
    dsl.deleteFrom(ul).where(ul.LIBRARY_ID.eq(libraryId)).execute()
    dsl.deleteFrom(l).where(l.ID.eq(libraryId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(ul).execute()
    dsl.deleteFrom(l).execute()
  }

  @Transactional
  override fun insert(library: Library) {
    dsl.insertInto(l)
      .set(l.ID, library.id)
      .set(l.NAME, library.name)
      .set(l.ROOT, library.root.toString())
      .set(l.IMPORT_COMICINFO_BOOK, library.importComicInfoBook)
      .set(l.IMPORT_COMICINFO_SERIES, library.importComicInfoSeries)
      .set(l.IMPORT_COMICINFO_COLLECTION, library.importComicInfoCollection)
      .set(l.IMPORT_COMICINFO_READLIST, library.importComicInfoReadList)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .set(l.IMPORT_MYLAR_SERIES, library.importMylarSeries)
      .set(l.IMPORT_LOCAL_ARTWORK, library.importLocalArtwork)
      .set(l.IMPORT_BARCODE_ISBN, library.importBarcodeIsbn)
      .set(l.SCAN_FORCE_MODIFIED_TIME, library.scanForceModifiedTime)
      .set(l.SCAN_DEEP, library.scanDeep)
      .set(l.REPAIR_EXTENSIONS, library.repairExtensions)
      .set(l.CONVERT_TO_CBZ, library.convertToCbz)
      .set(l.EMPTY_TRASH_AFTER_SCAN, library.emptyTrashAfterScan)
      .set(l.SERIES_COVER, library.seriesCover.toString())
      .set(l.HASH_FILES, library.hashFiles)
      .set(l.HASH_PAGES, library.hashPages)
      .set(l.ANALYZE_DIMENSIONS, library.analyzeDimensions)
      .set(l.UNAVAILABLE_DATE, library.unavailableDate)
      .execute()
  }

  @Transactional
  override fun update(library: Library) {
    dsl.update(l)
      .set(l.NAME, library.name)
      .set(l.ROOT, library.root.toString())
      .set(l.IMPORT_COMICINFO_BOOK, library.importComicInfoBook)
      .set(l.IMPORT_COMICINFO_SERIES, library.importComicInfoSeries)
      .set(l.IMPORT_COMICINFO_COLLECTION, library.importComicInfoCollection)
      .set(l.IMPORT_COMICINFO_READLIST, library.importComicInfoReadList)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .set(l.IMPORT_MYLAR_SERIES, library.importMylarSeries)
      .set(l.IMPORT_LOCAL_ARTWORK, library.importLocalArtwork)
      .set(l.IMPORT_BARCODE_ISBN, library.importBarcodeIsbn)
      .set(l.SCAN_FORCE_MODIFIED_TIME, library.scanForceModifiedTime)
      .set(l.SCAN_DEEP, library.scanDeep)
      .set(l.REPAIR_EXTENSIONS, library.repairExtensions)
      .set(l.CONVERT_TO_CBZ, library.convertToCbz)
      .set(l.EMPTY_TRASH_AFTER_SCAN, library.emptyTrashAfterScan)
      .set(l.SERIES_COVER, library.seriesCover.toString())
      .set(l.HASH_FILES, library.hashFiles)
      .set(l.HASH_PAGES, library.hashPages)
      .set(l.ANALYZE_DIMENSIONS, library.analyzeDimensions)
      .set(l.UNAVAILABLE_DATE, library.unavailableDate)
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
      importComicInfoReadList = importComicinfoReadlist,
      importEpubBook = importEpubBook,
      importEpubSeries = importEpubSeries,
      importMylarSeries = importMylarSeries,
      importLocalArtwork = importLocalArtwork,
      importBarcodeIsbn = importBarcodeIsbn,
      scanForceModifiedTime = scanForceModifiedTime,
      scanDeep = scanDeep,
      repairExtensions = repairExtensions,
      convertToCbz = convertToCbz,
      emptyTrashAfterScan = emptyTrashAfterScan,
      seriesCover = Library.SeriesCover.valueOf(seriesCover),
      hashFiles = hashFiles,
      hashPages = hashPages,
      analyzeDimensions = analyzeDimensions,

      unavailableDate = unavailableDate,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
    )
}
