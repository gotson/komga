package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.ReadlistRecord
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
import java.util.SortedMap

@Component
class ReadListDao(
  private val dsl: DSLContext
) : ReadListRepository {

  private val rl = Tables.READLIST
  private val rlb = Tables.READLIST_BOOK
  private val b = Tables.BOOK

  private val sorts = mapOf(
    "name" to DSL.lower(rl.NAME)
  )

  override fun findByIdOrNull(readListId: String): ReadList? =
    selectBase()
      .where(rl.ID.eq(readListId))
      .fetchAndMap(null)
      .firstOrNull()

  override fun findByIdOrNull(readListId: String, filterOnLibraryIds: Collection<String>?): ReadList? =
    selectBase()
      .where(rl.ID.eq(readListId))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .fetchAndMap(filterOnLibraryIds)
      .firstOrNull()

  override fun findAll(search: String?, pageable: Pageable): Page<ReadList> {
    val conditions = search?.let { rl.NAME.containsIgnoreCase(it) }
      ?: DSL.trueCondition()

    val count = dsl.selectCount()
      .from(rl)
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

  override fun findAllByLibraries(belongsToLibraryIds: Collection<String>, filterOnLibraryIds: Collection<String>?, search: String?, pageable: Pageable): Page<ReadList> {
    val ids = dsl.selectDistinct(rl.ID)
      .from(rl)
      .leftJoin(rlb).on(rl.ID.eq(rlb.READLIST_ID))
      .leftJoin(b).on(rlb.BOOK_ID.eq(b.ID))
      .where(b.LIBRARY_ID.`in`(belongsToLibraryIds))
      .apply { search?.let { and(rl.NAME.containsIgnoreCase(it)) } }
      .fetch(0, String::class.java)

    val count = ids.size

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items = selectBase()
      .where(rl.ID.`in`(ids))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .apply { search?.let { and(rl.NAME.containsIgnoreCase(it)) } }
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

  override fun findAllByBook(containsBookId: String, filterOnLibraryIds: Collection<String>?): Collection<ReadList> {
    val ids = dsl.select(rl.ID)
      .from(rl)
      .leftJoin(rlb).on(rl.ID.eq(rlb.READLIST_ID))
      .where(rlb.BOOK_ID.eq(containsBookId))
      .fetch(0, String::class.java)

    return selectBase()
      .where(rl.ID.`in`(ids))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .fetchAndMap(filterOnLibraryIds)
  }

  override fun findByNameOrNull(name: String): ReadList? =
    selectBase()
      .where(rl.NAME.equalIgnoreCase(name))
      .fetchAndMap(null)
      .firstOrNull()

  override fun findDeletedBooksByName(name: String): SortedMap<Int, String> =
    dsl.select(rlb.BOOK_ID, rlb.NUMBER)
      .from(rlb)
      .leftJoin(rl).on(rlb.READLIST_ID.eq(rl.ID))
      .where(rl.NAME.equalIgnoreCase(name))
      .and(rlb.DELETED.eq(true))
      .fetchInto(rlb).map { it.number to it.bookId }.toMap().toSortedMap()


  private fun selectBase() =
    dsl.selectDistinct(*rl.fields())
      .from(rl)
      .leftJoin(rlb).on(rl.ID.eq(rlb.READLIST_ID))
      .leftJoin(b).on(rlb.BOOK_ID.eq(b.ID))

  private fun ResultQuery<Record>.fetchAndMap(filterOnLibraryIds: Collection<String>?): List<ReadList> =
    fetchInto(rl)
      .map { rr ->
        val bookIds = dsl.select(*rlb.fields())
          .from(rlb)
          .leftJoin(b).on(rlb.BOOK_ID.eq(b.ID))
          .where(rlb.READLIST_ID.eq(rr.id))
          .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
          .orderBy(rlb.NUMBER.asc())
          .fetchInto(rlb)
          .mapNotNull { it.number to it.bookId }
          .toMap().toSortedMap()
        rr.toDomain(bookIds)
      }

  override fun insert(readList: ReadList) {
    dsl.transaction { config ->
      config.dsl().insertInto(rl)
        .set(rl.ID, readList.id)
        .set(rl.NAME, readList.name)
        .set(rl.BOOK_COUNT, readList.bookIds.size)
        .execute()

      insertBooks(config.dsl(), readList)
    }
  }

  private fun insertBooks(dsl: DSLContext, readList: ReadList) {
    readList.bookIds.map { (index, id) ->
      dsl.insertInto(rlb)
        .set(rlb.READLIST_ID, readList.id)
        .set(rlb.BOOK_ID, id)
        .set(rlb.NUMBER, index)
        .execute()
    }
  }

  override fun update(readList: ReadList) {
    dsl.transaction { config ->
      with(config.dsl()) {
        update(rl)
          .set(rl.NAME, readList.name)
          .set(rl.BOOK_COUNT, readList.bookIds.size)
          .set(rl.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
          .where(rl.ID.eq(readList.id))
          .execute()

        deleteFrom(rlb).where(rlb.READLIST_ID.eq(readList.id)).execute()

        insertBooks(config.dsl(), readList)
      }
    }
  }

  override fun removeBookFromAll(bookId: String) {
    dsl.deleteFrom(rlb)
      .where(rlb.BOOK_ID.eq(bookId))
      .execute()
  }

  override fun removeBookFromAll(bookIds: Collection<String>) {
    dsl.deleteFrom(rlb)
      .where(rlb.BOOK_ID.`in`(bookIds))
      .execute()
  }

  override fun softDeleteBookFromAll(bookIds: Collection<String>) {
    dsl.update(rlb)
      .set(rlb.DELETED, true)
      .where(rlb.BOOK_ID.`in`(bookIds))
      .execute()
  }

  override fun restoreDeletedBooksInAll(bookIds: Collection<String>) {
    dsl.update(rlb)
      .set(rlb.DELETED, false)
      .where(rlb.BOOK_ID.`in`(bookIds))
      .execute()
  }

  override fun delete(readListId: String) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(rlb).where(rlb.READLIST_ID.eq(readListId)).execute()
        deleteFrom(rl).where(rl.ID.eq(readListId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(rlb).execute()
        deleteFrom(rl).execute()
      }
    }
  }

  override fun existsByName(name: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(rl)
        .where(rl.NAME.equalIgnoreCase(name))
    )

  private fun ReadlistRecord.toDomain(bookIds: SortedMap<Int, String>) =
    ReadList(
      name = name,
      bookIds = bookIds,
      id = id,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      filtered = bookCount != bookIds.size
    )
}
