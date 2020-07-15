package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.jooq.Tables
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class KomgaUserDao(
  private val dsl: DSLContext
) : KomgaUserRepository {

  private val u = Tables.USER
  private val ul = Tables.USER_LIBRARY_SHARING

  override fun count(): Long = dsl.fetchCount(u).toLong()

  override fun findAll(): Collection<KomgaUser> =
    selectBase()
      .fetchAndMap()

  override fun findByIdOrNull(id: String): KomgaUser? =
    selectBase()
      .where(u.ID.equal(id))
      .fetchAndMap()
      .firstOrNull()

  private fun selectBase() =
    dsl
      .select(*u.fields())
      .select(ul.LIBRARY_ID)
      .from(u)
      .leftJoin(ul).onKey()

  private fun ResultQuery<Record>.fetchAndMap() =
    this.fetchGroups({ it.into(u) }, { it.into(ul) })
      .map { (ur, ulr) ->
        KomgaUser(
          email = ur.email,
          password = ur.password,
          roleAdmin = ur.roleAdmin,
          roleFileDownload = ur.roleFileDownload,
          rolePageStreaming = ur.rolePageStreaming,
          sharedLibrariesIds = ulr.mapNotNull { it.libraryId }.toSet(),
          sharedAllLibraries = ur.sharedAllLibraries,
          id = ur.id,
          createdDate = ur.createdDate.toCurrentTimeZone(),
          lastModifiedDate = ur.lastModifiedDate.toCurrentTimeZone()
        )
      }

  override fun insert(user: KomgaUser) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        insertInto(u)
          .set(u.ID, user.id)
          .set(u.EMAIL, user.email)
          .set(u.PASSWORD, user.password)
          .set(u.ROLE_ADMIN, user.roleAdmin)
          .set(u.ROLE_FILE_DOWNLOAD, user.roleFileDownload)
          .set(u.ROLE_PAGE_STREAMING, user.rolePageStreaming)
          .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
          .execute()

        user.sharedLibrariesIds.forEach {
          insertInto(ul)
            .columns(ul.USER_ID, ul.LIBRARY_ID)
            .values(user.id, it)
            .execute()
        }
      }
    }
  }

  override fun update(user: KomgaUser) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        update(u)
          .set(u.EMAIL, user.email)
          .set(u.PASSWORD, user.password)
          .set(u.ROLE_ADMIN, user.roleAdmin)
          .set(u.ROLE_FILE_DOWNLOAD, user.roleFileDownload)
          .set(u.ROLE_PAGE_STREAMING, user.rolePageStreaming)
          .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
          .set(u.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
          .where(u.ID.eq(user.id))
          .execute()

        deleteFrom(ul)
          .where(ul.USER_ID.eq(user.id))
          .execute()

        user.sharedLibrariesIds.forEach {
          insertInto(ul)
            .columns(ul.USER_ID, ul.LIBRARY_ID)
            .values(user.id, it)
            .execute()
        }
      }
    }
  }

  override fun delete(userId: String) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).where(ul.USER_ID.equal(userId)).execute()
        deleteFrom(u).where(u.ID.equal(userId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(ul).execute()
        deleteFrom(u).execute()
      }
    }
  }

  override fun existsByEmailIgnoreCase(email: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(u)
        .where(u.EMAIL.equalIgnoreCase(email))
    )

  override fun findByEmailIgnoreCase(email: String): KomgaUser? =
    selectBase()
      .where(u.EMAIL.equalIgnoreCase(email))
      .fetchAndMap()
      .firstOrNull()
}
