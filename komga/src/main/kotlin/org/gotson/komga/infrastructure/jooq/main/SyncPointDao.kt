package org.gotson.komga.infrastructure.jooq.main

import com.github.f4b6a3.tsid.TsidCreator
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.model.SyncPoint.ReadList.Companion.ON_DECK_ID
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.infrastructure.jooq.BookSearchHelper
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.language.toZonedDateTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SyncPointDao(
  private val dsl: DSLContext,
  private val bookCommonDao: BookCommonDao,
) : SyncPointRepository {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val bt = Tables.THUMBNAIL_BOOK
  private val r = Tables.READ_PROGRESS
  private val sd = Tables.SERIES_METADATA
  private val sp = Tables.SYNC_POINT
  private val spb = Tables.SYNC_POINT_BOOK
  private val spbs = Tables.SYNC_POINT_BOOK_REMOVED_SYNCED
  private val sprl = Tables.SYNC_POINT_READLIST
  private val sprlb = Tables.SYNC_POINT_READLIST_BOOK
  private val sprls = Tables.SYNC_POINT_READLIST_REMOVED_SYNCED

  @Transactional
  override fun create(
    apiKeyId: String?,
    search: BookSearch,
    context: SearchContext,
  ): SyncPoint {
    requireNotNull(context.userId) { "userId is required to create a SyncPoint" }

    val (condition, joins) = BookSearchHelper(context).toCondition(search.condition)

    val syncPointId = TsidCreator.getTsid256().toString()
    val createdAt = LocalDateTime.now(ZoneId.of("Z"))

    dsl
      .insertInto(
        sp,
        sp.ID,
        sp.USER_ID,
        sp.API_KEY_ID,
        sp.CREATED_DATE,
      ).values(
        syncPointId,
        context.userId,
        apiKeyId,
        createdAt,
      ).execute()

    dsl
      .insertInto(
        spb,
        spb.SYNC_POINT_ID,
        spb.BOOK_ID,
        spb.BOOK_CREATED_DATE,
        spb.BOOK_LAST_MODIFIED_DATE,
        spb.BOOK_FILE_LAST_MODIFIED,
        spb.BOOK_FILE_SIZE,
        spb.BOOK_FILE_HASH,
        spb.BOOK_METADATA_LAST_MODIFIED_DATE,
        spb.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE,
        spb.BOOK_THUMBNAIL_ID,
      ).select(
        dsl
          .select(
            DSL.`val`(syncPointId),
            b.ID,
            b.CREATED_DATE,
            b.LAST_MODIFIED_DATE,
            b.FILE_LAST_MODIFIED,
            b.FILE_SIZE,
            b.FILE_HASH,
            d.LAST_MODIFIED_DATE,
            r.LAST_MODIFIED_DATE,
            bt.ID,
          ).from(b)
          .apply {
            joins.forEach {
              when (it) {
                // for future work
                is RequiredJoin.ReadList -> Unit
                // we don't have to handle those since we already join on those tables anyway, the 'when' is here for future proofing
                RequiredJoin.BookMetadata -> Unit
                RequiredJoin.SeriesMetadata -> Unit
                RequiredJoin.Media -> Unit
                is RequiredJoin.ReadProgress -> Unit
                RequiredJoin.BookMetadataAggregation -> Unit
                is RequiredJoin.Collection -> Unit
              }
            }
          }.join(m)
          .on(b.ID.eq(m.BOOK_ID))
          .join(d)
          .on(b.ID.eq(d.BOOK_ID))
          .join(sd)
          .on(b.SERIES_ID.eq(sd.SERIES_ID))
          .leftJoin(r)
          .on(b.ID.eq(r.BOOK_ID))
          .and(r.USER_ID.eq(context.userId))
          .leftJoin(bt)
          .on(b.ID.eq(bt.BOOK_ID))
          .and(bt.SELECTED.isTrue)
          .where(condition),
      ).execute()

    return findByIdOrNull(syncPointId)!!
  }

  @Transactional
  override fun addOnDeck(
    syncPointId: String,
    context: SearchContext,
    filterOnLibraryIds: List<String>?,
  ) {
    requireNotNull(context.userId) { "Missing userId in search context" }

    val createdAt = LocalDateTime.now(ZoneId.of("Z"))
    val onDeckFields: Array<Field<*>> = arrayOf(DSL.`val`(syncPointId), DSL.`val`(ON_DECK_ID), b.ID)

    val (query, _, queryMostRecentDate) = bookCommonDao.getBooksOnDeckQuery(context.userId, context.restrictions, filterOnLibraryIds, onDeckFields)

    val count =
      dsl
        .insertInto(sprlb)
        .select(query)
        .execute()

    // only add the read list entry if some books were added
    if (count > 0) {
      val mostRecentDate = dsl.fetch(queryMostRecentDate).into(LocalDateTime::class.java).firstOrNull() ?: createdAt

      dsl
        .insertInto(
          sprl,
          sprl.SYNC_POINT_ID,
          sprl.READLIST_ID,
          sprl.READLIST_NAME,
          sprl.READLIST_CREATED_DATE,
          sprl.READLIST_LAST_MODIFIED_DATE,
        ).values(
          syncPointId,
          ON_DECK_ID,
          "On Deck",
          createdAt,
          mostRecentDate,
        ).execute()
    }
  }

  override fun findByIdOrNull(syncPointId: String): SyncPoint? =
    dsl
      .selectFrom(sp)
      .where(sp.ID.eq(syncPointId))
      .fetchInto(sp)
      .map {
        SyncPoint(
          id = it.id,
          userId = it.userId,
          apiKeyId = it.apiKeyId,
          createdDate = it.createdDate.toZonedDateTime(),
        )
      }.firstOrNull()

  override fun findBooksById(
    syncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl
        .selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(syncPointId))
        .apply {
          if (onlyNotSynced) {
            and(spb.SYNCED.isFalse)
          }
        }

    return queryToPageBook(query, pageable)
  }

  override fun findBooksAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl
        .selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(toSyncPointId))
        .apply {
          if (onlyNotSynced) {
            and(spb.SYNCED.isFalse)
          }
        }.and(
          spb.BOOK_ID.notIn(
            dsl.select(spb.BOOK_ID).from(spb).where(spb.SYNC_POINT_ID.eq(fromSyncPointId)),
          ),
        )

    return queryToPageBook(query, pageable)
  }

  override fun findBooksRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl
        .selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(fromSyncPointId))
        .and(
          spb.BOOK_ID.notIn(
            dsl.select(spb.BOOK_ID).from(spb).where(spb.SYNC_POINT_ID.eq(toSyncPointId)),
          ),
        ).apply {
          if (onlyNotSynced)
            and(
              spb.BOOK_ID.notIn(
                dsl.select(spbs.BOOK_ID).from(spbs).where(spbs.SYNC_POINT_ID.eq(toSyncPointId)),
              ),
            )
        }

    return queryToPageBook(query, pageable)
  }

  override fun findBooksChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val spbFrom = spb.`as`("spbFrom")
    val query =
      dsl
        .select(*spb.fields())
        .from(spb)
        .join(spbFrom)
        .on(spb.BOOK_ID.eq(spbFrom.BOOK_ID))
        .where(spb.SYNC_POINT_ID.eq(toSyncPointId))
        .and(spbFrom.SYNC_POINT_ID.eq(fromSyncPointId))
        .apply {
          if (onlyNotSynced) {
            and(spb.SYNCED.isFalse)
          }
        }.and(
          spb.BOOK_FILE_LAST_MODIFIED
            .ne(spbFrom.BOOK_FILE_LAST_MODIFIED)
            .or(spb.BOOK_FILE_SIZE.ne(spbFrom.BOOK_FILE_SIZE))
            .or(spb.BOOK_FILE_HASH.ne(spbFrom.BOOK_FILE_HASH).and(spbFrom.BOOK_FILE_HASH.isNotNull))
            .or(spb.BOOK_METADATA_LAST_MODIFIED_DATE.ne(spbFrom.BOOK_METADATA_LAST_MODIFIED_DATE))
            .or(spb.BOOK_THUMBNAIL_ID.ne(spbFrom.BOOK_THUMBNAIL_ID)),
        )

    return queryToPageBook(query, pageable)
  }

  override fun findBooksReadProgressChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val spbFrom = spb.`as`("spbFrom")
    val query =
      dsl
        .select(*spb.fields())
        .from(spb)
        .join(spbFrom)
        .on(spb.BOOK_ID.eq(spbFrom.BOOK_ID))
        .where(spb.SYNC_POINT_ID.eq(toSyncPointId))
        .and(spbFrom.SYNC_POINT_ID.eq(fromSyncPointId))
        .apply {
          if (onlyNotSynced) {
            and(spb.SYNCED.isFalse)
          }
        }.and(
          // unchanged book
          spb.BOOK_FILE_LAST_MODIFIED
            .eq(spbFrom.BOOK_FILE_LAST_MODIFIED)
            .and(spb.BOOK_FILE_SIZE.eq(spbFrom.BOOK_FILE_SIZE))
            .and(spb.BOOK_FILE_HASH.eq(spbFrom.BOOK_FILE_HASH).or(spbFrom.BOOK_FILE_HASH.isNull))
            .and(spb.BOOK_METADATA_LAST_MODIFIED_DATE.eq(spbFrom.BOOK_METADATA_LAST_MODIFIED_DATE))
            .and(spb.BOOK_THUMBNAIL_ID.eq(spbFrom.BOOK_THUMBNAIL_ID))
            // with changed read progress
            .and(
              spb.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE
                .ne(spbFrom.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE)
                .or(spb.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE.isNull.and(spbFrom.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE.isNotNull))
                .or(spb.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE.isNotNull.and(spbFrom.BOOK_READ_PROGRESS_LAST_MODIFIED_DATE.isNull)),
            ),
        )

    return queryToPageBook(query, pageable)
  }

  override fun findReadListsById(
    syncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> {
    val query =
      dsl
        .selectFrom(sprl)
        .where(sprl.SYNC_POINT_ID.eq(syncPointId))
        .apply {
          if (onlyNotSynced) {
            and(sprl.SYNCED.isFalse)
          }
        }

    return queryToPageReadList(query, pageable)
  }

  override fun findReadListsAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> {
    val to = sprl.`as`("to")
    val from = sprl.`as`("from")
    val query =
      dsl
        .select(*to.fields())
        .from(to)
        .leftOuterJoin(from)
        .on(to.READLIST_ID.eq(from.READLIST_ID).and(from.SYNC_POINT_ID.eq(fromSyncPointId)))
        .where(to.SYNC_POINT_ID.eq(toSyncPointId))
        .apply { if (onlyNotSynced) and(to.SYNCED.isFalse) }
        .and(from.READLIST_ID.isNull)

    return queryToPageReadList(query, pageable)
  }

  override fun findReadListsChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> {
    val from = sprl.`as`("from")
    val query =
      dsl
        .select(*sprl.fields())
        .from(sprl)
        .join(from)
        .on(sprl.READLIST_ID.eq(from.READLIST_ID))
        .where(sprl.SYNC_POINT_ID.eq(toSyncPointId))
        .and(from.SYNC_POINT_ID.eq(fromSyncPointId))
        .apply { if (onlyNotSynced) and(sprl.SYNCED.isFalse) }
        .and(
          sprl.READLIST_LAST_MODIFIED_DATE
            .ne(from.READLIST_LAST_MODIFIED_DATE)
            .or(sprl.READLIST_NAME.ne(from.READLIST_NAME)),
        )

    return queryToPageReadList(query, pageable)
  }

  override fun findReadListsRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> {
    val from = sprl.`as`("from")
    val to = sprl.`as`("to")
    val query =
      dsl
        .select(*from.fields())
        .from(from)
        .leftOuterJoin(to)
        .on(from.READLIST_ID.eq(to.READLIST_ID).and(to.SYNC_POINT_ID.eq(toSyncPointId)))
        .where(from.SYNC_POINT_ID.eq(fromSyncPointId))
        .apply {
          if (onlyNotSynced)
            and(
              from.READLIST_ID.notIn(
                dsl.select(sprls.READLIST_ID).from(sprls).where(sprls.SYNC_POINT_ID.eq(toSyncPointId)),
              ),
            )
        }.and(to.READLIST_ID.isNull)

    return queryToPageReadList(query, pageable)
  }

  override fun findBookIdsByReadListIds(
    syncPointId: String,
    readListIds: Collection<String>,
  ): List<SyncPoint.ReadList.Book> =
    dsl
      .select(*sprlb.fields())
      .from(sprlb)
      .where(sprlb.SYNC_POINT_ID.eq(syncPointId))
      .and(sprlb.READLIST_ID.`in`(readListIds))
      .fetchInto(sprlb)
      .map { SyncPoint.ReadList.Book(it.syncPointId, it.readlistId, it.bookId) }

  override fun markBooksSynced(
    syncPointId: String,
    forRemovedBooks: Boolean,
    bookIds: Collection<String>,
  ) {
    // removed books are not present in the 'to' SyncPoint, only in the 'from' SyncPoint
    // we store status in a separate table
    if (bookIds.isNotEmpty()) {
      if (forRemovedBooks)
        dsl
          .batch(
            dsl.insertInto(spbs, spbs.SYNC_POINT_ID, spbs.BOOK_ID).values(null as String?, null).onDuplicateKeyIgnore(),
          ).also { step ->
            bookIds.map { step.bind(syncPointId, it) }
          }.execute()
      else
        dsl
          .update(spb)
          .set(spb.SYNCED, true)
          .where(spb.SYNC_POINT_ID.eq(syncPointId))
          .and(spb.BOOK_ID.`in`(bookIds))
          .execute()
    }
  }

  override fun markReadListsSynced(
    syncPointId: String,
    forRemovedReadLists: Boolean,
    readListIds: Collection<String>,
  ) {
    // removed read lists are not present in the 'to' SyncPoint, only in the 'from' SyncPoint
    // we store status in a separate table
    if (readListIds.isNotEmpty()) {
      if (forRemovedReadLists)
        dsl
          .batch(
            dsl.insertInto(sprls, sprls.SYNC_POINT_ID, sprls.READLIST_ID).values(null as String?, null).onDuplicateKeyIgnore(),
          ).also { step ->
            readListIds.map { step.bind(syncPointId, it) }
          }.execute()
      else
        dsl
          .update(sprl)
          .set(sprl.SYNCED, true)
          .where(sprl.SYNC_POINT_ID.eq(syncPointId))
          .and(sprl.READLIST_ID.`in`(readListIds))
          .execute()
    }
  }

  override fun deleteByUserId(userId: String) {
    deleteSubEntities(dsl.select(sp.ID).from(sp).where(sp.USER_ID.eq(userId)))
    dsl.deleteFrom(sp).where(sp.USER_ID.eq(userId)).execute()
  }

  override fun deleteByUserIdAndApiKeyIds(
    userId: String,
    apiKeyIds: Collection<String>,
  ) {
    deleteSubEntities(dsl.select(sp.ID).from(sp).where(sp.USER_ID.eq(userId).and(sp.API_KEY_ID.`in`(apiKeyIds))))
    dsl.deleteFrom(sp).where(sp.USER_ID.eq(userId).and(sp.API_KEY_ID.`in`(apiKeyIds))).execute()
  }

  private fun deleteSubEntities(condition: SelectConditionStep<Record1<String>>) {
    dsl.deleteFrom(sprls).where(sprls.SYNC_POINT_ID.`in`(condition)).execute()
    dsl.deleteFrom(sprlb).where(sprlb.SYNC_POINT_ID.`in`(condition)).execute()
    dsl.deleteFrom(sprl).where(sprl.SYNC_POINT_ID.`in`(condition)).execute()
    dsl.deleteFrom(spbs).where(spbs.SYNC_POINT_ID.`in`(condition)).execute()
    dsl.deleteFrom(spb).where(spb.SYNC_POINT_ID.`in`(condition)).execute()
  }

  override fun deleteOne(syncPointId: String) {
    dsl.deleteFrom(sprls).where(sprls.SYNC_POINT_ID.eq(syncPointId)).execute()
    dsl.deleteFrom(sprlb).where(sprlb.SYNC_POINT_ID.eq(syncPointId)).execute()
    dsl.deleteFrom(sprl).where(sprl.SYNC_POINT_ID.eq(syncPointId)).execute()
    dsl.deleteFrom(spbs).where(spbs.SYNC_POINT_ID.eq(syncPointId)).execute()
    dsl.deleteFrom(spb).where(spb.SYNC_POINT_ID.eq(syncPointId)).execute()
    dsl.deleteFrom(sp).where(sp.ID.eq(syncPointId)).execute()
  }

  override fun deleteAll() {
    dsl.deleteFrom(sprls).execute()
    dsl.deleteFrom(sprlb).execute()
    dsl.deleteFrom(sprl).execute()
    dsl.deleteFrom(spbs).execute()
    dsl.deleteFrom(spb).execute()
    dsl.deleteFrom(sp).execute()
  }

  private fun queryToPageBook(
    query: SelectConditionStep<*>,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val count = dsl.fetchCount(query)

    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(spb)
        .map {
          SyncPoint.Book(
            syncPointId = it.syncPointId,
            bookId = it.bookId,
            createdDate = it.bookCreatedDate.atZone(ZoneId.of("Z")),
            lastModifiedDate = it.bookLastModifiedDate.atZone(ZoneId.of("Z")),
            fileLastModified = it.bookFileLastModified.atZone(ZoneId.of("Z")),
            fileSize = it.bookFileSize,
            fileHash = it.bookFileHash,
            metadataLastModifiedDate = it.bookMetadataLastModifiedDate.atZone(ZoneId.of("Z")),
            thumbnailId = it.bookThumbnailId,
            synced = it.synced,
          )
        }

    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.unsorted())
      else
        PageRequest.of(0, maxOf(count, 20), Sort.unsorted()),
      count.toLong(),
    )
  }

  private fun queryToPageReadList(
    query: SelectConditionStep<*>,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> {
    val count = dsl.fetchCount(query)

    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(sprl)
        .map {
          SyncPoint.ReadList(
            syncPointId = it.syncPointId,
            readListId = it.readlistId,
            readListName = it.readlistName,
            createdDate = it.readlistCreatedDate.atZone(ZoneId.of("Z")),
            lastModifiedDate = it.readlistLastModifiedDate.atZone(ZoneId.of("Z")),
            synced = it.synced,
          )
        }

    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.unsorted())
      else
        PageRequest.of(0, maxOf(count, 20), Sort.unsorted()),
      count.toLong(),
    )
  }
}
