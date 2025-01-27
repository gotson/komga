package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.AuthenticationActivity
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.gotson.komga.infrastructure.jooq.toOrderBy
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.AuthenticationActivityRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AuthenticationActivityDao(
  private val dsl: DSLContext,
) : AuthenticationActivityRepository {
  private val aa = Tables.AUTHENTICATION_ACTIVITY

  private val sorts =
    mapOf(
      "dateTime" to aa.DATE_TIME,
      "email" to aa.EMAIL,
      "success" to aa.SUCCESS,
      "ip" to aa.IP,
      "error" to aa.ERROR,
      "userId" to aa.USER_ID,
      "userAgent" to aa.USER_AGENT,
    )

  override fun findAll(pageable: Pageable): Page<AuthenticationActivity> {
    val conditions: Condition = DSL.trueCondition()
    return findAll(conditions, pageable)
  }

  override fun findAllByUser(
    user: KomgaUser,
    pageable: Pageable,
  ): Page<AuthenticationActivity> {
    val conditions = aa.USER_ID.eq(user.id).or(aa.EMAIL.eq(user.email))
    return findAll(conditions, pageable)
  }

  override fun findMostRecentByUser(
    user: KomgaUser,
    apiKeyId: String?,
  ): AuthenticationActivity? =
    dsl
      .selectFrom(aa)
      .where(aa.USER_ID.eq(user.id))
      .or(aa.EMAIL.eq(user.email))
      .apply { apiKeyId?.let { and(aa.API_KEY_ID.eq(it)) } }
      .orderBy(aa.DATE_TIME.desc())
      .limit(1)
      .fetchOne()
      ?.toDomain()

  private fun findAll(
    conditions: Condition,
    pageable: Pageable,
  ): PageImpl<AuthenticationActivity> {
    val count = dsl.fetchCount(aa, conditions)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items =
      dsl
        .selectFrom(aa)
        .where(conditions)
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(aa)
        .map { it.toDomain() }

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

  override fun insert(activity: AuthenticationActivity) {
    dsl
      .insertInto(aa, aa.USER_ID, aa.EMAIL, aa.API_KEY_ID, aa.API_KEY_COMMENT, aa.IP, aa.USER_AGENT, aa.SUCCESS, aa.ERROR, aa.SOURCE)
      .values(activity.userId, activity.email, activity.apiKeyId, activity.apiKeyComment, activity.ip, activity.userAgent, activity.success, activity.error, activity.source)
      .execute()
  }

  override fun deleteByUser(user: KomgaUser) {
    dsl
      .deleteFrom(aa)
      .where(aa.USER_ID.eq(user.id))
      .or(aa.EMAIL.eq(user.email))
      .execute()
  }

  override fun deleteOlderThan(dateTime: LocalDateTime) {
    dsl
      .deleteFrom(aa)
      .where(aa.DATE_TIME.lt(dateTime))
      .execute()
  }

  private fun AuthenticationActivityRecord.toDomain() =
    AuthenticationActivity(
      userId = userId,
      email = email,
      apiKeyId = apiKeyId,
      apiKeyComment = apiKeyComment,
      ip = ip,
      userAgent = userAgent,
      success = success,
      error = error,
      dateTime = dateTime.toCurrentTimeZone(),
      source = source,
    )
}
