package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ApiKey
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.AnnouncementsReadRecord
import org.gotson.komga.jooq.main.tables.records.UserApiKeyRecord
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
  private val ur = Tables.USER_ROLE
  private val ul = Tables.USER_LIBRARY_SHARING
  private val us = Tables.USER_SHARING
  private val ar = Tables.ANNOUNCEMENTS_READ
  private val uak = Tables.USER_API_KEY

  override fun count(): Long = dsl.fetchCount(u).toLong()

  override fun findAll(): Collection<KomgaUser> =
    selectBase()
      .fetchAndMap()

  override fun findApiKeyByUserId(userId: String): Collection<ApiKey> =
    dsl
      .selectFrom(uak)
      .where(uak.USER_ID.eq(userId))
      .fetchInto(uak)
      .map {
        it.toDomain()
      }

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
      .leftJoin(ul)
      .onKey()

  private fun ResultQuery<Record>.fetchAndMap() =
    this
      .fetchGroups({ it.into(u) }, { it.into(ul) })
      .map { (userRecord, ulr) ->
        val usr =
          dsl
            .selectFrom(us)
            .where(us.USER_ID.eq(userRecord.id))
            .toList()
        val roles =
          dsl
            .select(ur.ROLE)
            .from(ur)
            .where(ur.USER_ID.eq(userRecord.id))
            .fetch(ur.ROLE)
        KomgaUser(
          email = userRecord.email,
          password = userRecord.password,
          roles = UserRoles.valuesOf(roles),
          sharedLibrariesIds = ulr.mapNotNull { it.libraryId }.toSet(),
          sharedAllLibraries = userRecord.sharedAllLibraries,
          restrictions =
            ContentRestrictions(
              ageRestriction =
                if (userRecord.ageRestriction != null && userRecord.ageRestrictionAllowOnly != null)
                  AgeRestriction(userRecord.ageRestriction, if (userRecord.ageRestrictionAllowOnly) AllowExclude.ALLOW_ONLY else AllowExclude.EXCLUDE)
                else
                  null,
              labelsAllow = usr.filter { it.allow }.map { it.label }.toSet(),
              labelsExclude = usr.filterNot { it.allow }.map { it.label }.toSet(),
            ),
          id = userRecord.id,
          createdDate = userRecord.createdDate.toCurrentTimeZone(),
          lastModifiedDate = userRecord.lastModifiedDate.toCurrentTimeZone(),
        )
      }

  @Transactional
  override fun insert(user: KomgaUser) {
    dsl
      .insertInto(u)
      .set(u.ID, user.id)
      .set(u.EMAIL, user.email)
      .set(u.PASSWORD, user.password)
      .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
      .set(u.AGE_RESTRICTION, user.restrictions.ageRestriction?.age)
      .set(
        u.AGE_RESTRICTION_ALLOW_ONLY,
        when (user.restrictions.ageRestriction?.restriction) {
          AllowExclude.ALLOW_ONLY -> true
          AllowExclude.EXCLUDE -> false
          null -> null
        },
      ).execute()

    insertRoles(user)
    insertSharedLibraries(user)
    insertSharingRestrictions(user)
  }

  override fun insert(apiKey: ApiKey) {
    dsl
      .insertInto(uak)
      .set(uak.ID, apiKey.id)
      .set(uak.USER_ID, apiKey.userId)
      .set(uak.API_KEY, apiKey.key)
      .set(uak.COMMENT, apiKey.comment)
      .execute()
  }

  @Transactional
  override fun update(user: KomgaUser) {
    dsl
      .update(u)
      .set(u.EMAIL, user.email)
      .set(u.PASSWORD, user.password)
      .set(u.SHARED_ALL_LIBRARIES, user.sharedAllLibraries)
      .set(u.AGE_RESTRICTION, user.restrictions.ageRestriction?.age)
      .set(
        u.AGE_RESTRICTION_ALLOW_ONLY,
        when (user.restrictions.ageRestriction?.restriction) {
          AllowExclude.ALLOW_ONLY -> true
          AllowExclude.EXCLUDE -> false
          null -> null
        },
      ).set(u.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(u.ID.eq(user.id))
      .execute()

    dsl
      .deleteFrom(ur)
      .where(ur.USER_ID.eq(user.id))
      .execute()

    dsl
      .deleteFrom(ul)
      .where(ul.USER_ID.eq(user.id))
      .execute()

    dsl
      .deleteFrom(us)
      .where(us.USER_ID.eq(user.id))
      .execute()

    insertRoles(user)
    insertSharedLibraries(user)
    insertSharingRestrictions(user)
  }

  override fun saveAnnouncementIdsRead(
    user: KomgaUser,
    announcementIds: Set<String>,
  ) {
    dsl.batchStore(announcementIds.map { AnnouncementsReadRecord(user.id, it) }).execute()
  }

  private fun insertRoles(user: KomgaUser) {
    user.roles.forEach {
      dsl
        .insertInto(ur)
        .columns(ur.USER_ID, ur.ROLE)
        .values(user.id, it.name)
        .execute()
    }
  }

  private fun insertSharedLibraries(user: KomgaUser) {
    user.sharedLibrariesIds.forEach {
      dsl
        .insertInto(ul)
        .columns(ul.USER_ID, ul.LIBRARY_ID)
        .values(user.id, it)
        .execute()
    }
  }

  private fun insertSharingRestrictions(user: KomgaUser) {
    user.restrictions.labelsAllow.forEach { label ->
      dsl
        .insertInto(us)
        .columns(us.USER_ID, us.ALLOW, us.LABEL)
        .values(user.id, true, label)
        .execute()
    }

    user.restrictions.labelsExclude.forEach { label ->
      dsl
        .insertInto(us)
        .columns(us.USER_ID, us.ALLOW, us.LABEL)
        .values(user.id, false, label)
        .execute()
    }
  }

  @Transactional
  override fun delete(userId: String) {
    dsl.deleteFrom(uak).where(uak.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(ar).where(ar.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(us).where(us.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(ul).where(ul.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(ur).where(ur.USER_ID.equal(userId)).execute()
    dsl.deleteFrom(u).where(u.ID.equal(userId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(uak).execute()
    dsl.deleteFrom(ar).execute()
    dsl.deleteFrom(us).execute()
    dsl.deleteFrom(ul).execute()
    dsl.deleteFrom(ur).execute()
    dsl.deleteFrom(u).execute()
  }

  override fun deleteApiKeyByIdAndUserId(
    apiKeyId: String,
    userId: String,
  ) {
    dsl
      .deleteFrom(uak)
      .where(uak.ID.eq(apiKeyId))
      .and(uak.USER_ID.eq(userId))
      .execute()
  }

  override fun deleteApiKeyByUserId(userId: String) {
    dsl.deleteFrom(uak).where(uak.USER_ID.eq(userId)).execute()
  }

  override fun findAnnouncementIdsReadByUserId(userId: String): Set<String> =
    dsl
      .select(ar.ANNOUNCEMENT_ID)
      .from(ar)
      .where(ar.USER_ID.eq(userId))
      .fetchSet(ar.ANNOUNCEMENT_ID)

  override fun existsByEmailIgnoreCase(email: String): Boolean =
    dsl.fetchExists(
      dsl
        .selectFrom(u)
        .where(u.EMAIL.equalIgnoreCase(email)),
    )

  override fun existsApiKeyByIdAndUserId(
    apiKeyId: String,
    userId: String,
  ): Boolean = dsl.fetchExists(uak, uak.ID.eq(apiKeyId).and(uak.USER_ID.eq(userId)))

  override fun existsApiKeyByCommentAndUserId(
    comment: String,
    userId: String,
  ): Boolean = dsl.fetchExists(uak, uak.COMMENT.equalIgnoreCase(comment).and(uak.USER_ID.eq(userId)))

  override fun findByEmailIgnoreCaseOrNull(email: String): KomgaUser? =
    selectBase()
      .where(u.EMAIL.equalIgnoreCase(email))
      .fetchAndMap()
      .firstOrNull()

  override fun findByApiKeyOrNull(apiKey: String): Pair<KomgaUser, ApiKey>? {
    val user =
      selectBase()
        .leftJoin(uak)
        .on(u.ID.eq(uak.USER_ID))
        .where(uak.API_KEY.eq(apiKey))
        .fetchAndMap()
        .firstOrNull() ?: return null

    val key =
      dsl
        .selectFrom(uak)
        .where(uak.API_KEY.eq(apiKey))
        .fetchInto(uak)
        .map { it.toDomain() }
        .firstOrNull() ?: return null

    return Pair(user, key)
  }

  private fun UserApiKeyRecord.toDomain() =
    ApiKey(
      id = id,
      userId = userId,
      key = apiKey,
      comment = comment,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
    )
}
