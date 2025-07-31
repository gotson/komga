package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.inOrNoCondition
import org.gotson.komga.infrastructure.jooq.sortByValues
import org.gotson.komga.infrastructure.jooq.toCondition
import org.gotson.komga.infrastructure.jooq.toSortField
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.CollectionRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.beans.factory.annotation.Qualifier
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

@Component
class SeriesCollectionDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  private val luceneHelper: LuceneHelper,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SeriesCollectionRepository {
  private val c = Tables.COLLECTION
  private val cs = Tables.COLLECTION_SERIES
  private val s = Tables.SERIES
  private val sd = Tables.SERIES_METADATA

  private val sorts =
    mapOf(
      "name" to c.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
    )

  override fun findByIdOrNull(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): SeriesCollection? =
    dslRO
      .selectBase(restrictions.isRestricted)
      .where(c.ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
      .fetchAndMap(dslRO, filterOnLibraryIds, restrictions)
      .firstOrNull()

  override fun findAll(
    belongsToLibraryIds: Collection<String>?,
    filterOnLibraryIds: Collection<String>?,
    search: String?,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<SeriesCollection> {
    val collectionIds = luceneHelper.searchEntitiesIds(search, LuceneEntity.Collection)
    val searchCondition = c.ID.inOrNoCondition(collectionIds)

    val conditions =
      searchCondition
        .and(s.LIBRARY_ID.inOrNoCondition(belongsToLibraryIds))
        .and(s.LIBRARY_ID.inOrNoCondition(filterOnLibraryIds))
        .and(restrictions.toCondition())

    val queryIds =
      if (belongsToLibraryIds == null && filterOnLibraryIds == null && !restrictions.isRestricted)
        null
      else
        dslRO
          .selectDistinct(c.ID)
          .from(c)
          .leftJoin(cs)
          .on(c.ID.eq(cs.COLLECTION_ID))
          .leftJoin(s)
          .on(cs.SERIES_ID.eq(s.ID))
          .leftJoin(sd)
          .on(cs.SERIES_ID.eq(sd.SERIES_ID))
          .where(conditions)

    val count =
      if (queryIds != null)
        dslRO.fetchCount(queryIds)
      else
        dslRO.fetchCount(c, searchCondition)

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !collectionIds.isNullOrEmpty())
          c.ID.sortByValues(collectionIds, it.isAscending)
        else
          it.toSortField(sorts)
      }

    val items =
      dslRO
        .selectBase(restrictions.isRestricted)
        .where(conditions)
        .apply { if (queryIds != null) and(c.ID.`in`(queryIds)) }
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(dslRO, filterOnLibraryIds, restrictions)

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

  override fun findAllContainingSeriesId(
    containsSeriesId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): Collection<SeriesCollection> {
    val queryIds =
      dslRO
        .select(c.ID)
        .from(c)
        .leftJoin(cs)
        .on(c.ID.eq(cs.COLLECTION_ID))
        .apply { if (restrictions.isRestricted) leftJoin(sd).on(cs.SERIES_ID.eq(sd.SERIES_ID)) }
        .where(cs.SERIES_ID.eq(containsSeriesId))
        .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }

    return dslRO
      .selectBase(restrictions.isRestricted)
      .where(c.ID.`in`(queryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
      .fetchAndMap(dslRO, filterOnLibraryIds, restrictions)
  }

  override fun findAllEmpty(): Collection<SeriesCollection> =
    dslRO
      .selectFrom(c)
      .where(
        c.ID.`in`(
          dslRO
            .select(c.ID)
            .from(c)
            .leftJoin(cs)
            .on(c.ID.eq(cs.COLLECTION_ID))
            .where(cs.COLLECTION_ID.isNull),
        ),
      ).fetchInto(c)
      .map { it.toDomain(emptyList()) }

  override fun findByNameOrNull(name: String): SeriesCollection? =
    dslRO
      .selectBase()
      .where(c.NAME.equalIgnoreCase(name))
      .fetchAndMap(dslRO, null)
      .firstOrNull()

  private fun DSLContext.selectBase(joinOnSeriesMetadata: Boolean = false) =
    this
      .selectDistinct(*c.fields())
      .from(c)
      .leftJoin(cs)
      .on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s)
      .on(cs.SERIES_ID.eq(s.ID))
      .apply { if (joinOnSeriesMetadata) leftJoin(sd).on(cs.SERIES_ID.eq(sd.SERIES_ID)) }

  private fun ResultQuery<Record>.fetchAndMap(
    dsl: DSLContext,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): List<SeriesCollection> =
    fetchInto(c)
      .map { cr ->
        val seriesIds =
          dsl
            .select(*cs.fields())
            .from(cs)
            .leftJoin(s)
            .on(cs.SERIES_ID.eq(s.ID))
            .apply { if (restrictions.isRestricted) leftJoin(sd).on(cs.SERIES_ID.eq(sd.SERIES_ID)) }
            .where(cs.COLLECTION_ID.eq(cr.id))
            .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
            .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
            .orderBy(cs.NUMBER.asc())
            .fetchInto(cs)
            .mapNotNull { it.seriesId }
        cr.toDomain(seriesIds)
      }

  @Transactional
  override fun insert(collection: SeriesCollection) {
    dslRW
      .insertInto(c)
      .set(c.ID, collection.id)
      .set(c.NAME, collection.name)
      .set(c.ORDERED, collection.ordered)
      .set(c.SERIES_COUNT, collection.seriesIds.size)
      .execute()

    dslRW.insertSeries(collection)
  }

  private fun DSLContext.insertSeries(collection: SeriesCollection) {
    collection.seriesIds.forEachIndexed { index, id ->
      this
        .insertInto(cs)
        .set(cs.COLLECTION_ID, collection.id)
        .set(cs.SERIES_ID, id)
        .set(cs.NUMBER, index)
        .execute()
    }
  }

  @Transactional
  override fun update(collection: SeriesCollection) {
    dslRW
      .update(c)
      .set(c.NAME, collection.name)
      .set(c.ORDERED, collection.ordered)
      .set(c.SERIES_COUNT, collection.seriesIds.size)
      .set(c.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(c.ID.eq(collection.id))
      .execute()

    dslRW.deleteFrom(cs).where(cs.COLLECTION_ID.eq(collection.id)).execute()

    dslRW.insertSeries(collection)
  }

  @Transactional
  override fun removeSeriesFromAll(seriesId: String) {
    dslRW
      .deleteFrom(cs)
      .where(cs.SERIES_ID.eq(seriesId))
      .execute()
  }

  @Transactional
  override fun removeSeriesFromAll(seriesIds: Collection<String>) {
    dslRW.withTempTable(batchSize, seriesIds).use {
      dslRW
        .deleteFrom(cs)
        .where(cs.SERIES_ID.`in`(it.selectTempStrings()))
        .execute()
    }
  }

  @Transactional
  override fun delete(collectionId: String) {
    dslRW.deleteFrom(cs).where(cs.COLLECTION_ID.eq(collectionId)).execute()
    dslRW.deleteFrom(c).where(c.ID.eq(collectionId)).execute()
  }

  @Transactional
  override fun delete(collectionIds: Collection<String>) {
    dslRW.deleteFrom(cs).where(cs.COLLECTION_ID.`in`(collectionIds)).execute()
    dslRW.deleteFrom(c).where(c.ID.`in`(collectionIds)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dslRW.deleteFrom(cs).execute()
    dslRW.deleteFrom(c).execute()
  }

  override fun existsByName(name: String): Boolean =
    dslRO.fetchExists(
      dslRO
        .selectFrom(c)
        .where(c.NAME.equalIgnoreCase(name)),
    )

  override fun count(): Long = dslRO.fetchCount(c).toLong()

  private fun CollectionRecord.toDomain(seriesIds: List<String>) =
    SeriesCollection(
      name = name,
      ordered = ordered,
      seriesIds = seriesIds,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      filtered = seriesCount != seriesIds.size,
    )
}
