package org.gotson.komga.infrastructure.jooq.main

import com.github.f4b6a3.tsid.TsidCreator
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.infrastructure.jooq.toCondition
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
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

@Component
class SyncPointDao(
  private val dsl: DSLContext,
) : SyncPointRepository {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val sd = Tables.SERIES_METADATA
  private val sp = Tables.SYNC_POINT
  private val spb = Tables.SYNC_POINT_BOOK

  @Transactional
  override fun create(
    user: KomgaUser,
    search: BookSearch,
  ): SyncPoint {
    val conditions = search.toCondition().and(user.restrictions.toCondition(dsl))

    val syncPointId = TsidCreator.getTsid256().toString()
    val createdAt = LocalDateTime.now()

    dsl.insertInto(
      sp,
      sp.ID,
      sp.USER_ID,
      sp.CREATED_DATE,
    ).values(
      syncPointId,
      user.id,
      createdAt,
    ).execute()

    dsl.insertInto(
      spb,
      spb.SYNC_POINT_ID,
      spb.BOOK_ID,
      spb.BOOK_FILE_LAST_MODIFIED,
      spb.BOOK_FILE_SIZE,
      spb.BOOK_FILE_HASH,
      spb.BOOK_METADATA_LAST_MODIFIED_DATE,
    ).select(
      dsl.select(
        DSL.`val`(syncPointId),
        b.ID,
        b.FILE_LAST_MODIFIED,
        b.FILE_SIZE,
        b.FILE_HASH,
        d.LAST_MODIFIED_DATE,
      ).from(b)
        .join(m).on(b.ID.eq(m.BOOK_ID))
        .join(d).on(b.ID.eq(d.BOOK_ID))
        .join(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
        .where(conditions),
    ).execute()

    return findByIdOrNull(syncPointId)!!
  }

  override fun findByIdOrNull(syncPointId: String): SyncPoint? =
    dsl.selectFrom(sp)
      .where(sp.ID.eq(syncPointId))
      .fetchInto(sp)
      .map {
        SyncPoint(
          id = it.id,
          userId = it.userId,
          createdDate = it.createdDate,
        )
      }.firstOrNull()

  override fun findBooksById(
    syncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl.selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(syncPointId))

    return queryToPage(query, pageable)
  }

  override fun findBooksAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl.selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(toSyncPointId))
        .and(
          spb.BOOK_ID.notIn(
            dsl.select(spb.BOOK_ID).from(spb).where(spb.SYNC_POINT_ID.eq(fromSyncPointId)),
          ),
        )

    return queryToPage(query, pageable)
  }

  override fun findBooksRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val query =
      dsl.selectFrom(spb)
        .where(spb.SYNC_POINT_ID.eq(fromSyncPointId))
        .and(
          spb.BOOK_ID.notIn(
            dsl.select(spb.BOOK_ID).from(spb).where(spb.SYNC_POINT_ID.eq(toSyncPointId)),
          ),
        )

    return queryToPage(query, pageable)
  }

  override fun findBooksChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> {
    val spbFrom = spb.`as`("spbFrom")
    val query =
      dsl.select(*spb.fields())
        .from(spb)
        .join(spbFrom).on(spb.BOOK_ID.eq(spbFrom.BOOK_ID))
        .where(spb.SYNC_POINT_ID.eq(toSyncPointId))
        .and(spbFrom.SYNC_POINT_ID.eq(fromSyncPointId))
        .and(
          spb.BOOK_FILE_LAST_MODIFIED.ne(spbFrom.BOOK_FILE_LAST_MODIFIED)
            .or(spb.BOOK_FILE_SIZE.ne(spbFrom.BOOK_FILE_SIZE))
            .or(spb.BOOK_FILE_HASH.ne(spbFrom.BOOK_FILE_HASH).and(spbFrom.BOOK_FILE_HASH.isNotNull))
            .or(spb.BOOK_METADATA_LAST_MODIFIED_DATE.ne(spbFrom.BOOK_METADATA_LAST_MODIFIED_DATE)),
        )

    return queryToPage(query, pageable)
  }

  override fun deleteByUserId(userId: String) {
    dsl.deleteFrom(spb).where(
      spb.SYNC_POINT_ID.`in`(
        dsl.select(sp.ID).from(sp).where(sp.USER_ID.eq(userId)),
      ),
    ).execute()
    dsl.deleteFrom(sp).where(sp.USER_ID.eq(userId)).execute()
  }

  override fun deleteAll() {
    dsl.deleteFrom(spb).execute()
    dsl.deleteFrom(sp).execute()
  }

  private fun queryToPage(
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
            fileLastModified = it.bookFileLastModified,
            fileSize = it.bookFileSize,
            fileHash = it.bookFileHash,
            metadataLastModifiedDate = it.bookMetadataLastModifiedDate,
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
