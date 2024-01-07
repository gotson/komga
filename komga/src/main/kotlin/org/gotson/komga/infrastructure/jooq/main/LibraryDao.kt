package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.LibraryRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
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
  private val le = Tables.LIBRARY_EXCLUSIONS

  override fun findByIdOrNull(libraryId: String): Library? =
    findOne(libraryId)
      .fetchAndMap()
      .firstOrNull()

  override fun findById(libraryId: String): Library =
    findOne(libraryId)
      .fetchAndMap()
      .first()

  private fun findOne(libraryId: String) =
    selectBase()
      .where(l.ID.eq(libraryId))
  override fun findAll(): Collection<Library> =
    selectBase()
      .fetchAndMap()

  override fun findAllByIds(libraryIds: Collection<String>): Collection<Library> =
    selectBase()
      .where(l.ID.`in`(libraryIds))
      .fetchAndMap()

  private fun selectBase() =
    dsl.select()
      .from(l)
      .leftJoin(le).onKey()

  private fun ResultQuery<Record>.fetchAndMap(): Collection<Library> =
    this.fetchGroups({ it.into(l) }, { it.into(le) })
      .map { (lr, ler) ->
        lr.toDomain(ler.mapNotNull { it.exclusion }.toSet())
      }

  @Transactional
  override fun delete(libraryId: String) {
    dsl.deleteFrom(le).where(le.LIBRARY_ID.eq(libraryId)).execute()
    dsl.deleteFrom(ul).where(ul.LIBRARY_ID.eq(libraryId)).execute()
    dsl.deleteFrom(l).where(l.ID.eq(libraryId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(le).execute()
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
      .set(l.IMPORT_COMICINFO_SERIES_APPEND_VOLUME, library.importComicInfoSeriesAppendVolume)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .set(l.IMPORT_MYLAR_SERIES, library.importMylarSeries)
      .set(l.IMPORT_LOCAL_ARTWORK, library.importLocalArtwork)
      .set(l.IMPORT_BARCODE_ISBN, library.importBarcodeIsbn)
      .set(l.SCAN_FORCE_MODIFIED_TIME, library.scanForceModifiedTime)
      .set(l.SCAN_CBX, library.scanCbx)
      .set(l.SCAN_PDF, library.scanPdf)
      .set(l.SCAN_EPUB, library.scanEpub)
      .set(l.SCAN_STARTUP, library.scanOnStartup)
      .set(l.SCAN_INTERVAL, library.scanInterval.toString())
      .set(l.REPAIR_EXTENSIONS, library.repairExtensions)
      .set(l.CONVERT_TO_CBZ, library.convertToCbz)
      .set(l.EMPTY_TRASH_AFTER_SCAN, library.emptyTrashAfterScan)
      .set(l.SERIES_COVER, library.seriesCover.toString())
      .set(l.SERIES_SORT, library.seriesSort.toString())
      .set(l.HASH_FILES, library.hashFiles)
      .set(l.HASH_PAGES, library.hashPages)
      .set(l.ANALYZE_DIMENSIONS, library.analyzeDimensions)
      .set(l.ONESHOTS_DIRECTORY, library.oneshotsDirectory)
      .set(l.UNAVAILABLE_DATE, library.unavailableDate)
      .execute()

    insertDirectoryExclusions(library)
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
      .set(l.IMPORT_COMICINFO_SERIES_APPEND_VOLUME, library.importComicInfoSeriesAppendVolume)
      .set(l.IMPORT_EPUB_BOOK, library.importEpubBook)
      .set(l.IMPORT_EPUB_SERIES, library.importEpubSeries)
      .set(l.IMPORT_MYLAR_SERIES, library.importMylarSeries)
      .set(l.IMPORT_LOCAL_ARTWORK, library.importLocalArtwork)
      .set(l.IMPORT_BARCODE_ISBN, library.importBarcodeIsbn)
      .set(l.SCAN_FORCE_MODIFIED_TIME, library.scanForceModifiedTime)
      .set(l.SCAN_CBX, library.scanCbx)
      .set(l.SCAN_PDF, library.scanPdf)
      .set(l.SCAN_EPUB, library.scanEpub)
      .set(l.SCAN_STARTUP, library.scanOnStartup)
      .set(l.SCAN_INTERVAL, library.scanInterval.toString())
      .set(l.REPAIR_EXTENSIONS, library.repairExtensions)
      .set(l.CONVERT_TO_CBZ, library.convertToCbz)
      .set(l.EMPTY_TRASH_AFTER_SCAN, library.emptyTrashAfterScan)
      .set(l.SERIES_COVER, library.seriesCover.toString())
      .set(l.SERIES_SORT, library.seriesSort.toString())
      .set(l.HASH_FILES, library.hashFiles)
      .set(l.HASH_PAGES, library.hashPages)
      .set(l.ANALYZE_DIMENSIONS, library.analyzeDimensions)
      .set(l.ONESHOTS_DIRECTORY, library.oneshotsDirectory)
      .set(l.UNAVAILABLE_DATE, library.unavailableDate)
      .set(l.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(l.ID.eq(library.id))
      .execute()

    dsl.deleteFrom(le).where(le.LIBRARY_ID.eq(library.id)).execute()
    insertDirectoryExclusions(library)
  }

  override fun count(): Long = dsl.fetchCount(l).toLong()
  fun findDirectoryExclusions(libraryId: String): Set<String> =
    dsl.select(le.EXCLUSION)
      .from(le)
      .where(le.LIBRARY_ID.eq(libraryId))
      .fetchSet(le.EXCLUSION)

  private fun insertDirectoryExclusions(library: Library) {
    if (library.scanDirectoryExclusions.isNotEmpty()) {
      dsl.batch(
        dsl.insertInto(le, le.LIBRARY_ID, le.EXCLUSION)
          .values(null as String?, null),
      ).also { step ->
        library.scanDirectoryExclusions.forEach {
          step.bind(library.id, it)
        }
      }.execute()
    }
  }

  private fun LibraryRecord.toDomain(directoryExclusions: Set<String>) =
    Library(
      name = name,
      root = URL(root),
      importComicInfoBook = importComicinfoBook,
      importComicInfoSeries = importComicinfoSeries,
      importComicInfoCollection = importComicinfoCollection,
      importComicInfoReadList = importComicinfoReadlist,
      importComicInfoSeriesAppendVolume = importComicinfoSeriesAppendVolume,
      importEpubBook = importEpubBook,
      importEpubSeries = importEpubSeries,
      importMylarSeries = importMylarSeries,
      importLocalArtwork = importLocalArtwork,
      importBarcodeIsbn = importBarcodeIsbn,
      scanForceModifiedTime = scanForceModifiedTime,
      scanCbx = scanCbx,
      scanPdf = scanPdf,
      scanEpub = scanEpub,
      scanOnStartup = scanStartup,
      scanInterval = Library.ScanInterval.valueOf(scanInterval),
      scanDirectoryExclusions = directoryExclusions,
      repairExtensions = repairExtensions,
      convertToCbz = convertToCbz,
      emptyTrashAfterScan = emptyTrashAfterScan,
      seriesCover = Library.SeriesCover.valueOf(seriesCover),
      seriesSort = Library.SeriesSort.valueOf(seriesSort),
      hashFiles = hashFiles,
      hashPages = hashPages,
      analyzeDimensions = analyzeDimensions,
      oneshotsDirectory = oneshotsDirectory,

      unavailableDate = unavailableDate,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
    )
}
