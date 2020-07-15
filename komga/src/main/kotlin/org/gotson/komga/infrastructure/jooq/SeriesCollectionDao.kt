package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.CollectionRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesCollectionDao(
  private val dsl: DSLContext
) : SeriesCollectionRepository {

  private val c = Tables.COLLECTION
  private val cs = Tables.COLLECTION_SERIES
  private val s = Tables.SERIES

  private val sorts = mapOf(
    "name" to DSL.lower(c.NAME)
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
    val conditions = search?.let { c.NAME.containsIgnoreCase(it) }
      ?: DSL.trueCondition()

    val count = dsl.selectCount()
      .from(c)
      .where(conditions)
      .fetchOne(0, Long::class.java)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items = selectBase()
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchAndMap(null)

    val pageSort = if (orderBy.size > 1) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count.toInt(), 20), pageSort),
      count.toLong()
    )
  }

  override fun findAllByLibraries(belongsToLibraryIds: Collection<String>, filterOnLibraryIds: Collection<String>?, search: String?, pageable: Pageable): Page<SeriesCollection> {
    val ids = dsl.selectDistinct(c.ID)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(belongsToLibraryIds))
      .apply { search?.let { and(c.NAME.containsIgnoreCase(it)) } }
      .fetch(0, String::class.java)

    val count = ids.size

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items = selectBase()
      .where(c.ID.`in`(ids))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .apply { search?.let { and(c.NAME.containsIgnoreCase(it)) } }
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchAndMap(filterOnLibraryIds)

    val pageSort = if (orderBy.size > 1) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong()
    )
  }

  override fun findAllBySeries(containsSeriesId: String, filterOnLibraryIds: Collection<String>?): Collection<SeriesCollection> {
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

  override fun insert(collection: SeriesCollection) {
    dsl.transaction { config ->
      config.dsl().insertInto(c)
        .set(c.ID, collection.id)
        .set(c.NAME, collection.name)
        .set(c.ORDERED, collection.ordered)
        .set(c.SERIES_COUNT, collection.seriesIds.size)
        .execute()

      insertSeries(config.dsl(), collection)
    }
  }


  private fun insertSeries(dsl: DSLContext, collection: SeriesCollection) {
    collection.seriesIds.forEachIndexed { index, id ->
      dsl.insertInto(cs)
        .set(cs.COLLECTION_ID, collection.id)
        .set(cs.SERIES_ID, id)
        .set(cs.NUMBER, index)
        .execute()
    }
  }

  override fun update(collection: SeriesCollection) {
    dsl.transaction { config ->
      with(config.dsl()) {
        update(c)
          .set(c.NAME, collection.name)
          .set(c.ORDERED, collection.ordered)
          .set(c.SERIES_COUNT, collection.seriesIds.size)
          .set(c.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
          .where(c.ID.eq(collection.id))
          .execute()

        deleteFrom(cs).where(cs.COLLECTION_ID.eq(collection.id)).execute()

        insertSeries(config.dsl(), collection)
      }
    }
  }

  override fun removeSeriesFromAll(seriesId: String) {
    dsl.deleteFrom(cs)
      .where(cs.SERIES_ID.eq(seriesId))
      .execute()
  }

  override fun removeSeriesFromAll(seriesIds: Collection<String>) {
    dsl.deleteFrom(cs)
      .where(cs.SERIES_ID.`in`(seriesIds))
      .execute()
  }

  override fun delete(collectionId: String) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(cs).where(cs.COLLECTION_ID.eq(collectionId)).execute()
        deleteFrom(c).where(c.ID.eq(collectionId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(cs).execute()
        deleteFrom(c).execute()
      }
    }
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
