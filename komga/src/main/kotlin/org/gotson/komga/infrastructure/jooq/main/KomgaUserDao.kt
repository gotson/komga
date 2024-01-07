package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.AnnouncementsReadRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class KomgaUserDao(
  private val dsl: DSLContext,
) : KomgaUserRepository {

  private val u = Tables.USER
  private val ul = Tables.USER_LIBRARY_SHARING
  private val us = Tables.USER_SHARING
  private val ar = Tables.ANNOUNCEMENTS_READ

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
        val usr = dsl.selectFrom(us)
          .where(us.USER_ID.eq(ur.id))
          .toList()
        KomgaUser(
          email = ur.email,
          password = ur.password,
          roleAdmin = ur.roleAdmin,
          roleFileDownload = ur.roleFileDownload,
          rolePageStreaming = ur.rolePageStreaming,
          sharedLibrariesIds = ulr.mapNotNull { it.libraryId }.toSet(),
          sharedAllLibraries = ur.sharedAllLibraries,
          restrictions = ContentRestrictions(
            ageRestriction = if (ur.ageRestriction != null && ur.ageRestrictionAllowOnly != null)
              AgeRestriction(ur.ageRestriction, if (ur.ageRestrictionAllowOnly) AllowExclude.ALLOW_ONLY else AllowExclude.EXCLUDE)
            else null,
            labelsAllow = usr.filter { it.allow }.map { it.label }.toSet(),
            labelsExclude = usr.filterNot { it.allow }.map { it.label }.toSet(),
          ),
          id = ur.id,
          createdDate = ur.createdDate.toCurrentTimeZone(),
          lastModifiedDate = ur.lastModifiedDate.toCurrentTimeZone(),
        )
      }

  @Transactional
  override fun insert(user: KomgaUser) {
    dsl.insertInto(u)
      .set(u.ID, user.id)
      .set(u.EMAIL, user.email)
      .set(u.PASSWORD, user.password)
      .set(u.ROLE_ADMIN, user.roleAdmin)
      .set(u.ROLE_FILE_DOWNLOAD, user.roleFileDownload)
      .set(u.ROLE_PAGE_STREAMING, user.rolePageStreaming)
      .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
      .set(u.AGE_RESTRICTION, user.restrictions.ageRestriction?.age)
      .set(
        u.AGE_RESTRICTION_ALLOW_ONLY,
        when (user.restrictions.ageRestriction?.restriction) {
          AllowExclude.ALLOW_ONLY -> true
          AllowExclude.EXCLUDE -> false
          null -> null
        },
      )
      .execute()

    insertSharedLibraries(user)
    insertSharingRestrictions(user)
  }

  @Transactional
  override fun update(user: KomgaUser) {
    dsl.update(u)
      .set(u.EMAIL, user.email)
      .set(u.PASSWORD, user.password)
      .set(u.ROLE_ADMIN, user.roleAdmin)
      .set(u.ROLE_FILE_DOWNLOAD, user.roleFileDownload)
      .set(u.ROLE_PAGE_STREAMING, user.rolePageStreaming)
      .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
      .set(u.AGE_RESTRICTION, user.restrictions.ageRestriction?.age)
      .set(
        u.AGE_RESTRICTION_ALLOW_ONLY,
        when (user.restrictions.ageRestriction?.restriction) {
          AllowExclude.ALLOW_ONLY -> true
          AllowExclude.EXCLUDE -> false
          null -> null
        },
      )
      .set(u.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(u.ID.eq(user.id))
      .execute()

    dsl.deleteFrom(ul)
      .where(ul.USER_ID.eq(user.id))
      .execute()

    dsl.deleteFrom(us)
      .where(us.USER_ID.eq(user.id))
      .execute()

    insertSharedLibraries(user)
    insertSharingRestrictions(user)
  }

  override fun saveAnnouncementIdsRead(user: KomgaUser, announcementIds: Set<String>) {
    dsl.batchStore(announcementIds.map { AnnouncementsReadRecord(user.id, it) }).execute()
  }

  private fun insertSharedLibraries(user: KomgaUser) {
    user.sharedLibrariesIds.forEach {
      dsl.insertInto(ul)
        .columns(ul.USER_ID, ul.LIBRARY_ID)
        .values(user.id, it)
        .execute()
    }
  }

  private fun insertSharingRestrictions(user: KomgaUser) {
    user.restrictions.labelsAllow.forEach { label ->
      dsl.insertInto(us)
        .columns(us.USER_ID, us.ALLOW, us.LABEL)
        .values(user.id, true, label)
        .execute()
    }

    user.restrictions.labelsExclude.forEach { label ->
      dsl.insertInto(us)
        .columns(us.USER_ID, us.ALLOW, us.LABEL)
        .values(user.id, false, label)
        .execute()
    }
  }

  @Transactional
  override fun delete(userId: String) {
    dsl.deleteFrom(ar).where(ar.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(us).where(us.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(ul).where(ul.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(u).where(u.ID.equal(userId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(ar).execute()
    dsl.deleteFrom(us).execute()
    dsl.deleteFrom(ul).execute()
    dsl.deleteFrom(u).execute()
  }

  override fun findAnnouncementIdsReadByUserId(userId: String): Set<String> =
    dsl.select(ar.ANNOUNCEMENT_ID)
      .from(ar)
      .where(ar.USER_ID.eq(userId))
      .fetchSet(ar.ANNOUNCEMENT_ID)

  override fun existsByEmailIgnoreCase(email: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(u)
        .where(u.EMAIL.equalIgnoreCase(email)),
    )

  override fun findByEmailIgnoreCaseOrNull(email: String): KomgaUser? =
    selectBase()
      .where(u.EMAIL.equalIgnoreCase(email))
      .fetchAndMap()
      .firstOrNull()
}
