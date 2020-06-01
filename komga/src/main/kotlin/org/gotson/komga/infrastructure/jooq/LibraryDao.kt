package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.jooq.Sequences.HIBERNATE_SEQUENCE
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.LibraryRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.net.URL

@Component
class LibraryDao(
  private val dsl: DSLContext
) : LibraryRepository {

  private val l = Tables.LIBRARY
  private val ul = Tables.USER_LIBRARY_SHARING

  override fun findByIdOrNull(libraryId: Long): Library? =
    dsl.selectFrom(l)
      .where(l.ID.eq(libraryId))
      .fetchOneInto(l)
      ?.toDomain()

  override fun findAll(): Collection<Library> =
    dsl.selectFrom(l)
      .fetchInto(l)
      .map { it.toDomain() }

  override fun findAllById(libraryIds: Collection<Long>): Collection<Library> =
    dsl.selectFrom(l)
      .where(l.ID.`in`(libraryIds))
      .fetchInto(l)
      .map { it.toDomain() }

  override fun existsByName(name: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(l)
        .where(l.NAME.equalIgnoreCase(name))
    )

  override fun delete(libraryId: Long) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).where(ul.LIBRARY_ID.eq(libraryId)).execute()
        deleteFrom(l).where(l.ID.eq(libraryId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).execute()
        deleteFrom(l).execute()
      }
    }
  }

  override fun insert(library: Library): Library {
    val id = dsl.nextval(HIBERNATE_SEQUENCE)

    dsl.insertInto(l)
      .set(l.ID, id)
      .set(l.NAME, library.name)
      .set(l.ROOT, library.root.toString())
      .execute()

    return findByIdOrNull(id)!!
  }

  override fun count(): Long = dsl.fetchCount(l).toLong()


  private fun LibraryRecord.toDomain() =
    Library(
      name = name,
      root = URL(root),
      id = id,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )
}
