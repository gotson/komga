package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.CollectionRecord
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

@Component
class SeriesCollectionDao(
  private val dsl: DSLContext,
  private val luceneHelper: LuceneHelper,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SeriesCollectionRepository {

  private val c = Tables.COLLECTION
  private val cs = Tables.COLLECTION_SERIES
  private val s = Tables.SERIES

  private val sorts = mapOf(
    "name" to c.NAME.collate(SqliteUdfDataSource.collationUnicode3),
  )

  override fun findByIdOrNull(collectionId: String): SeriesCollection? =
    selectBase()
      .where(c.ID.eq(collectionId))
      .fetchAndMap(null)
      .firstOrNull()

  override fun findByIdOrNull(collectionId: String, filterOnLibraryIds: Collection<String>?): SeriesCollection? =
    selectBase()
      .where(c.ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .fetchAndMap(filterOnLibraryIds)
      .firstOrNull()

  override fun findAll(search: String?, pageable: Pageable): Page<SeriesCollection> {
    val collectionIds = luceneHelper.searchEntitiesIds(search, LuceneEntity.Collection)
    val searchCondition = c.ID.inOrNoCondition(collectionIds)

    val count = dsl.selectCount()
      .from(c)
      .where(searchCondition)
      .fetchOne(0, Long::class.java) ?: 0

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !collectionIds.isNullOrEmpty()) c.ID.sortByValues(collectionIds, it.isAscending)
        else it.toSortField(sorts)
      }

    val items = selectBase()
      .where(searchCondition)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchAndMap(null)

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count.toInt(), 20), pageSort),
      count
    )
  }

  override fun findAllByLibraryIds(belongsToLibraryIds: Collection<String>, filterOnLibraryIds: Collection<String>?, search: String?, pageable: Pageable): Page<SeriesCollection> {
    val collectionIds = luceneHelper.searchEntitiesIds(search, LuceneEntity.Collection)
    val searchCondition = c.ID.inOrNoCondition(collectionIds)

    val conditions = s.LIBRARY_ID.`in`(belongsToLibraryIds)
      .and(searchCondition)
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }

    val ids = dsl.selectDistinct(c.ID)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))
      .where(conditions)
      .fetch(0, String::class.java)

    val count = ids.size

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !collectionIds.isNullOrEmpty()) c.ID.sortByValues(collectionIds, it.isAscending)
        else it.toSortField(sorts)
      }

    val items = selectBase()
      .where(c.ID.`in`(ids))
      .and(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchAndMap(filterOnLibraryIds)

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong()
    )
  }

  override fun findAllContainingSeriesId(containsSeriesId: String, filterOnLibraryIds: Collection<String>?): Collection<SeriesCollection> {
    val ids = dsl.select(c.ID)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .where(cs.SERIES_ID.eq(containsSeriesId))
      .fetch(0, String::class.java)

    return selectBase()
      .where(c.ID.`in`(ids))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .fetchAndMap(filterOnLibraryIds)
  }

  override fun findAllEmpty(): Collection<SeriesCollection> =
    dsl.selectFrom(c)
      .where(
        c.ID.`in`(
          dsl.select(c.ID)
            .from(c)
            .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
            .where(cs.COLLECTION_ID.isNull)
        )
      ).fetchInto(c)
      .map { it.toDomain(emptyList()) }

  override fun findByNameOrNull(name: String): SeriesCollection? =
    selectBase()
      .where(c.NAME.equalIgnoreCase(name))
      .fetchAndMap(null)
      .firstOrNull()

  private fun selectBase() =
    dsl.selectDistinct(*c.fields())
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))

  private fun ResultQuery<Record>.fetchAndMap(filterOnLibraryIds: Collection<String>?): List<SeriesCollection> =
    fetchInto(c)
      .map { cr ->
        val seriesIds = dsl.select(*cs.fields())
          .from(cs)
          .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))
          .where(cs.COLLECTION_ID.eq(cr.id))
          .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
          .orderBy(cs.NUMBER.asc())
          .fetchInto(cs)
          .mapNotNull { it.seriesId }
        cr.toDomain(seriesIds)
      }

  @Transactional
  override fun insert(collection: SeriesCollection) {
    dsl.insertInto(c)
      .set(c.ID, collection.id)
      .set(c.NAME, collection.name)
      .set(c.ORDERED, collection.ordered)
      .set(c.SERIES_COUNT, collection.seriesIds.size)
      .execute()

    insertSeries(collection)
  }

  private fun insertSeries(collection: SeriesCollection) {
    collection.seriesIds.forEachIndexed { index, id ->
      dsl.insertInto(cs)
        .set(cs.COLLECTION_ID, collection.id)
        .set(cs.SERIES_ID, id)
        .set(cs.NUMBER, index)
        .execute()
    }
  }

  @Transactional
  override fun update(collection: SeriesCollection) {
    dsl.update(c)
      .set(c.NAME, collection.name)
      .set(c.ORDERED, collection.ordered)
      .set(c.SERIES_COUNT, collection.seriesIds.size)
      .set(c.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(c.ID.eq(collection.id))
      .execute()

    dsl.deleteFrom(cs).where(cs.COLLECTION_ID.eq(collection.id)).execute()

    insertSeries(collection)
  }

  @Transactional
  override fun removeSeriesFromAll(seriesId: String) {
    dsl.deleteFrom(cs)
      .where(cs.SERIES_ID.eq(seriesId))
      .execute()
  }

  @Transactional
  override fun removeSeriesFromAll(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(cs)
      .where(cs.SERIES_ID.`in`(dsl.selectTempStrings()))
      .execute()
  }

  @Transactional
  override fun delete(collectionId: String) {

    dsl.deleteFrom(cs).where(cs.COLLECTION_ID.eq(collectionId)).execute()
    dsl.deleteFrom(c).where(c.ID.eq(collectionId)).execute()
  }

  @Transactional
  override fun delete(collectionIds: Collection<String>) {
    dsl.deleteFrom(cs).where(cs.COLLECTION_ID.`in`(collectionIds)).execute()
    dsl.deleteFrom(c).where(c.ID.`in`(collectionIds)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(cs).execute()
    dsl.deleteFrom(c).execute()
  }

  override fun existsByName(name: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(c)
        .where(c.NAME.equalIgnoreCase(name))
    )

  private fun CollectionRecord.toDomain(seriesIds: List<String>) =
    SeriesCollection(
      name = name,
      ordered = ordered,
      seriesIds = seriesIds,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      filtered = seriesCount != seriesIds.size
    )
}
