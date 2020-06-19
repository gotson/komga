package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.jooq.Sequences
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.CollectionRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SeriesCollectionDao(
  private val dsl: DSLContext
) : SeriesCollectionRepository {

  private val c = Tables.COLLECTION
  private val cs = Tables.COLLECTION_SERIES
  private val s = Tables.SERIES

  private val groupFields = arrayOf(*c.fields(), *cs.fields())


  override fun findByIdOrNull(collectionId: Long): SeriesCollection? =
    selectBase()
      .where(c.ID.eq(collectionId))
      .groupBy(*groupFields)
      .orderBy(cs.NUMBER.asc())
      .fetchAndMap()
      .firstOrNull()

  override fun findByIdOrNull(collectionId: Long, filterOnLibraryIds: Collection<Long>?): SeriesCollection? =
    selectBase()
      .where(c.ID.eq(collectionId))
      .also { step ->
        filterOnLibraryIds?.let { step.and(s.LIBRARY_ID.`in`(it)) }
      }
      .groupBy(*groupFields)
      .orderBy(cs.NUMBER.asc())
      .fetchAndMap()
      .firstOrNull()

  override fun findAll(): Collection<SeriesCollection> =
    selectBase()
      .groupBy(*groupFields)
      .orderBy(cs.NUMBER.asc())
      .fetchAndMap()

  override fun findAllByLibraries(belongsToLibraryIds: Collection<Long>, filterOnLibraryIds: Collection<Long>?): Collection<SeriesCollection> {
    val ids = dsl.select(c.ID)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(belongsToLibraryIds))
      .fetch(0, Long::class.java)

    return selectBase()
      .where(c.ID.`in`(ids))
      .also { step ->
        filterOnLibraryIds?.let { step.and(s.LIBRARY_ID.`in`(it)) }
      }
      .groupBy(*groupFields)
      .orderBy(cs.NUMBER.asc())
      .fetchAndMap()
  }

  override fun findAllBySeries(containsSeriesId: Long, filterOnLibraryIds: Collection<Long>?): Collection<SeriesCollection> {
    val ids = dsl.select(c.ID)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .where(cs.SERIES_ID.eq(containsSeriesId))
      .fetch(0, Long::class.java)

    return selectBase()
      .where(c.ID.`in`(ids))
      .also { step ->
        filterOnLibraryIds?.let { step.and(s.LIBRARY_ID.`in`(it)) }
      }
      .groupBy(*groupFields)
      .orderBy(cs.NUMBER.asc())
      .fetchAndMap()
  }

  private fun selectBase() =
    dsl.select(*groupFields)
      .from(c)
      .leftJoin(cs).on(c.ID.eq(cs.COLLECTION_ID))
      .leftJoin(s).on(cs.SERIES_ID.eq(s.ID))

  private fun ResultQuery<Record>.fetchAndMap() =
    fetchGroups({ it.into(c) }, { it.into(cs) })
      .map { (cr, csr) ->
        val seriesIds = csr.map { it.seriesId }
        cr.toDomain(seriesIds)
      }

  override fun insert(collection: SeriesCollection): SeriesCollection {
    val id = dsl.nextval(Sequences.HIBERNATE_SEQUENCE)
    val insert = collection.copy(id = id)

    dsl.insertInto(c)
      .set(c.ID, insert.id)
      .set(c.NAME, insert.name)
      .set(c.ORDERED, insert.ordered)
      .set(c.SERIES_COUNT, collection.seriesIds.size)
      .execute()

    insertSeries(insert)

    return findByIdOrNull(id)!!
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

  override fun update(collection: SeriesCollection) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        update(c)
          .set(c.NAME, collection.name)
          .set(c.ORDERED, collection.ordered)
          .set(c.SERIES_COUNT, collection.seriesIds.size)
          .set(c.LAST_MODIFIED_DATE, LocalDateTime.now())
          .where(c.ID.eq(collection.id))
          .execute()

        deleteFrom(cs).where(cs.COLLECTION_ID.eq(collection.id)).execute()

        insertSeries(collection)
      }
    }
  }

  override fun removeSeriesFromAll(seriesId: Long) {
    dsl.deleteFrom(cs)
      .where(cs.SERIES_ID.eq(seriesId))
      .execute()
  }

  override fun delete(collectionId: Long) {
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


  private fun CollectionRecord.toDomain(seriesIds: List<Long>) =
    SeriesCollection(
      name = name,
      ordered = ordered,
      seriesIds = seriesIds,
      id = id,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate,
      filtered = seriesCount != seriesIds.size
    )
}
