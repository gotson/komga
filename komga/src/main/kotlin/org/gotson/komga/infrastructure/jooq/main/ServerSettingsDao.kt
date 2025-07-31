package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class ServerSettingsDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
) {
  private val s = Tables.SERVER_SETTINGS

  fun <T> getSettingByKey(
    key: String,
    clazz: Class<T>,
  ): T? =
    dslRO
      .select(s.VALUE)
      .from(s)
      .where(s.KEY.eq(key))
      .fetchOneInto(clazz)

  fun saveSetting(
    key: String,
    value: String,
  ) {
    dslRW
      .insertInto(s)
      .values(key, value)
      .onDuplicateKeyUpdate()
      .set(s.VALUE, value)
      .execute()
  }

  fun saveSetting(
    key: String,
    value: Boolean,
  ) {
    saveSetting(key, value.toString())
  }

  fun saveSetting(
    key: String,
    value: Int,
  ) {
    saveSetting(key, value.toString())
  }

  fun deleteSetting(key: String) {
    dslRW.deleteFrom(s).where(s.KEY.eq(key)).execute()
  }

  fun deleteAll() {
    dslRW.deleteFrom(s).execute()
  }
}
