package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.interfaces.api.rest.dto.ClientSettingDto
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class ClientSettingsDtoDao(
  private val dsl: DSLContext,
) {
  private val g = Tables.CLIENT_SETTINGS_GLOBAL
  private val u = Tables.CLIENT_SETTINGS_USER

  fun findAllGlobal(onlyUnauthorized: Boolean = false): Map<String, ClientSettingDto> =
    dsl
      .selectFrom(g)
      .apply { if (onlyUnauthorized) where(g.ALLOW_UNAUTHORIZED.isTrue) }
      .fetch()
      .associate { it.key to ClientSettingDto(it.value, it.allowUnauthorized) }

  fun findAllUser(userId: String): Map<String, ClientSettingDto> =
    dsl
      .selectFrom(u)
      .where(u.USER_ID.eq(userId))
      .fetch()
      .associate { it.key to ClientSettingDto(it.value, null) }

  fun saveGlobal(
    key: String,
    value: String,
    allowUnauthorized: Boolean,
  ) {
    dsl
      .insertInto(g, g.KEY, g.VALUE, g.ALLOW_UNAUTHORIZED)
      .values(key, value, allowUnauthorized)
      .onDuplicateKeyUpdate()
      .set(g.VALUE, value)
      .execute()
  }

  fun saveForUser(
    userId: String,
    key: String,
    value: String,
  ) {
    dsl
      .insertInto(u, u.USER_ID, u.KEY, u.VALUE)
      .values(userId, key, value)
      .onDuplicateKeyUpdate()
      .set(u.VALUE, value)
      .execute()
  }

  fun deleteAll() {
    dsl.deleteFrom(g).execute()
    dsl.deleteFrom(u).execute()
  }

  fun deleteGlobalByKeys(keys: Collection<String>) {
    dsl
      .deleteFrom(g)
      .where(g.KEY.`in`(keys))
      .execute()
  }

  fun deleteByUserIdAndKeys(
    userId: String,
    keys: Collection<String>,
  ) {
    dsl
      .deleteFrom(u)
      .where(u.KEY.`in`(keys))
      .and(u.USER_ID.eq(userId))
      .execute()
  }

  fun deleteByUserId(userId: String) {
    dsl
      .deleteFrom(u)
      .where(u.USER_ID.eq(userId))
      .execute()
  }
}
