package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.inOrNoCondition
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.infrastructure.jooq.sortByValues
import org.gotson.komga.infrastructure.jooq.toCondition
import org.gotson.komga.infrastructure.jooq.toSortField
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ReadlistRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.SortedMap

@Component
class ReadListDao(
  private val dsl: DSLContext,
  private val luceneHelper: LuceneHelper,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : ReadListRepository {
  private val rl = Tables.READLIST
  private val rlb = Tables.READLIST_BOOK
  private val b = Tables.BOOK
  private val sd = Tables.SERIES_METADATA

  private val sorts =
    mapOf(
      "name" to rl.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "createdDate" to rl.CREATED_DATE,
      "lastModifiedDate" to rl.LAST_MODIFIED_DATE,
    )

  override fun findByIdOrNull(
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): ReadList? =
    selectBase(restrictions.isRestricted)
      .where(rl.ID.eq(readListId))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
      .fetchAndMap(filterOnLibraryIds, restrictions)
      .firstOrNull()

  override fun findAll(
    belongsToLibraryIds: Collection<String>?,
    filterOnLibraryIds: Collection<String>?,
    search: String?,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<ReadList> {
    val readListIds = luceneHelper.searchEntitiesIds(search, LuceneEntity.ReadList)
    val searchCondition = rl.ID.inOrNoCondition(readListIds)

    val conditions =
      searchCondition
        .and(b.LIBRARY_ID.inOrNoCondition(belongsToLibraryIds))
        .and(b.LIBRARY_ID.inOrNoCondition(filterOnLibraryIds))
        .and(restrictions.toCondition())

    val queryIds =
      if (belongsToLibraryIds == null && filterOnLibraryIds == null && !restrictions.isRestricted)
        null
      else
        dsl
          .selectDistinct(rl.ID)
          .from(rl)
          .leftJoin(rlb)
          .on(rl.ID.eq(rlb.READLIST_ID))
          .leftJoin(b)
          .on(rlb.BOOK_ID.eq(b.ID))
          .apply { if (restrictions.isRestricted) leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }
          .where(conditions)

    val count =
      if (queryIds != null)
        dsl.fetchCount(queryIds)
      else
        dsl.fetchCount(rl, searchCondition)

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !readListIds.isNullOrEmpty())
          rl.ID.sortByValues(readListIds, it.isAscending)
        else
          it.toSortField(sorts)
      }

    val items =
      selectBase(restrictions.isRestricted)
        .where(conditions)
        .apply { if (queryIds != null) and(rl.ID.`in`(queryIds)) }
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(filterOnLibraryIds, restrictions)

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findAllContainingBookId(
    containsBookId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): Collection<ReadList> {
    val queryIds =
      dsl
        .select(rl.ID)
        .from(rl)
        .leftJoin(rlb)
        .on(rl.ID.eq(rlb.READLIST_ID))
        .apply { if (restrictions.isRestricted) leftJoin(b).on(rlb.BOOK_ID.eq(b.ID)).leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }
        .where(rlb.BOOK_ID.eq(containsBookId))
        .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }

    return selectBase(restrictions.isRestricted)
      .where(rl.ID.`in`(queryIds))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
      .fetchAndMap(filterOnLibraryIds, restrictions)
  }

  override fun findAllEmpty(): Collection<ReadList> =
    dsl
      .selectFrom(rl)
      .where(
        rl.ID.`in`(
          dsl
            .select(rl.ID)
            .from(rl)
            .leftJoin(rlb)
            .on(rl.ID.eq(rlb.READLIST_ID))
            .where(rlb.READLIST_ID.isNull),
        ),
      ).fetchInto(rl)
      .map { it.toDomain(sortedMapOf()) }

  override fun findByNameOrNull(name: String): ReadList? =
    selectBase()
      .where(rl.NAME.equalIgnoreCase(name))
      .fetchAndMap(null)
      .firstOrNull()

  private fun selectBase(joinOnSeriesMetadata: Boolean = false) =
    dsl
      .selectDistinct(*rl.fields())
      .from(rl)
      .leftJoin(rlb)
      .on(rl.ID.eq(rlb.READLIST_ID))
      .leftJoin(b)
      .on(rlb.BOOK_ID.eq(b.ID))
      .apply { if (joinOnSeriesMetadata) leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }

  private fun ResultQuery<Record>.fetchAndMap(
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): List<ReadList> =
    fetchInto(rl)
      .map { rr ->
        val bookIds =
          dsl
            .select(*rlb.fields())
            .from(rlb)
            .leftJoin(b)
            .on(rlb.BOOK_ID.eq(b.ID))
            .apply { if (restrictions.isRestricted) leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }
            .where(rlb.READLIST_ID.eq(rr.id))
            .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
            .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
            .orderBy(rlb.NUMBER.asc())
            .fetchInto(rlb)
            .mapNotNull { it.number to it.bookId }
            .toMap()
            .toSortedMap()
        rr.toDomain(bookIds)
      }

  @Transactional
  override fun insert(readList: ReadList) {
    dsl
      .insertInto(rl)
      .set(rl.ID, readList.id)
      .set(rl.NAME, readList.name)
      .set(rl.SUMMARY, readList.summary)
      .set(rl.ORDERED, readList.ordered)
      .set(rl.BOOK_COUNT, readList.bookIds.size)
      .execute()

    insertBooks(readList)
  }

  private fun insertBooks(readList: ReadList) {
    readList.bookIds.map { (index, id) ->
      dsl
        .insertInto(rlb)
        .set(rlb.READLIST_ID, readList.id)
        .set(rlb.BOOK_ID, id)
        .set(rlb.NUMBER, index)
        .execute()
    }
  }

  @Transactional
  override fun update(readList: ReadList) {
    dsl
      .update(rl)
      .set(rl.NAME, readList.name)
      .set(rl.SUMMARY, readList.summary)
      .set(rl.ORDERED, readList.ordered)
      .set(rl.BOOK_COUNT, readList.bookIds.size)
      .set(rl.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(rl.ID.eq(readList.id))
      .execute()

    dsl.deleteFrom(rlb).where(rlb.READLIST_ID.eq(readList.id)).execute()

    insertBooks(readList)
  }

  override fun removeBookFromAll(bookId: String) {
    dsl
      .deleteFrom(rlb)
      .where(rlb.BOOK_ID.eq(bookId))
      .execute()
  }

  @Transactional
  override fun removeBooksFromAll(bookIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, bookIds)

    dsl
      .deleteFrom(rlb)
      .where(rlb.BOOK_ID.`in`(dsl.selectTempStrings()))
      .execute()
  }

  @Transactional
  override fun delete(readListId: String) {
    dsl.deleteFrom(rlb).where(rlb.READLIST_ID.eq(readListId)).execute()
    dsl.deleteFrom(rl).where(rl.ID.eq(readListId)).execute()
  }

  @Transactional
  override fun delete(readListIds: Collection<String>) {
    dsl.deleteFrom(rlb).where(rlb.READLIST_ID.`in`(readListIds)).execute()
    dsl.deleteFrom(rl).where(rl.ID.`in`(readListIds)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(rlb).execute()
    dsl.deleteFrom(rl).execute()
  }

  override fun existsByName(name: String): Boolean =
    dsl.fetchExists(
      dsl
        .selectFrom(rl)
        .where(rl.NAME.equalIgnoreCase(name)),
    )

  override fun count(): Long = dsl.fetchCount(rl).toLong()

  private fun ReadlistRecord.toDomain(bookIds: SortedMap<Int, String>) =
    ReadList(
      name = name,
      summary = summary,
      ordered = ordered,
      bookIds = bookIds,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      filtered = bookCount != bookIds.size,
    )
}
