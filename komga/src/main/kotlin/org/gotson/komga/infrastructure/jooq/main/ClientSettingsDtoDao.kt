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

  fun findAllGlobal(): Collection<ClientSettingDto> =
    dsl.selectFrom(g)
      .map { ClientSettingDto(it.key, it.value, it.allowUnauthorized, null) }

  fun findAllUser(userId: String): Collection<ClientSettingDto> =
    dsl.selectFrom(u)
      .where(u.USER_ID.eq(userId))
      .map { ClientSettingDto(it.key, it.value, null, it.userId) }

  fun saveGlobal(key: String, value: String, allowUnauthorized: Boolean) {
    dsl
      .insertInto(g, g.KEY, g.VALUE, g.ALLOW_UNAUTHORIZED)
      .values(key, value, allowUnauthorized)
      .onDuplicateKeyUpdate()
      .set(g.VALUE, value)
      .execute()
  }

  fun saveForUser(userId: String, key: String, value: String) {
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

  fun deleteGlobalByKey(key: String) {
    dsl
      .deleteFrom(g)
      .where(g.KEY.eq(key))
      .execute()
  }

  fun deleteByUserIdAndKey(userId: String, key: String) {
    dsl
      .deleteFrom(u)
      .where(u.KEY.eq(key))
      .and(u.USER_ID.eq(userId))
      .execute()
  }

  fun deleteByUserId(userId: String) {
    dsl.deleteFrom(u)
      .where(u.USER_ID.eq(userId))
      .execute()
  }
}
