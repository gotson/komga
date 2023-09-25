package org.gotson.komga.infrastructure.configuration

import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.infrastructure.jooq.ServerSettingsDao
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Service
class KomgaSettingsProvider(
  private val serverSettingsDao: ServerSettingsDao,
) {
  var deleteEmptyCollections: Boolean
    get() =
      serverSettingsDao.getSettingByKey(Settings.DELETE_EMPTY_COLLECTIONS.name, Boolean::class.java) ?: false
    set(value) =
      serverSettingsDao.saveSetting(Settings.DELETE_EMPTY_COLLECTIONS.name, value)

  var deleteEmptyReadLists: Boolean
    get() =
      serverSettingsDao.getSettingByKey(Settings.DELETE_EMPTY_READLISTS.name, Boolean::class.java) ?: false
    set(value) =
      serverSettingsDao.saveSetting(Settings.DELETE_EMPTY_READLISTS.name, value)

  var rememberMeKey: String
    get() =
      serverSettingsDao.getSettingByKey(Settings.REMEMBER_ME_KEY.name, String::class.java)
        ?: getRandomRememberMeKey().also { serverSettingsDao.saveSetting(Settings.REMEMBER_ME_KEY.name, it) }
    set(value) =
      serverSettingsDao.saveSetting(Settings.REMEMBER_ME_KEY.name, value)

  fun renewRememberMeKey() {
    serverSettingsDao.saveSetting(Settings.REMEMBER_ME_KEY.name, getRandomRememberMeKey())
  }

  private fun getRandomRememberMeKey() = RandomStringUtils.randomAlphanumeric(32)

  var rememberMeDuration: Duration
    get() =
      (serverSettingsDao.getSettingByKey(Settings.REMEMBER_ME_DURATION.name, Int::class.java) ?: 365).days
    set(value) =
      serverSettingsDao.saveSetting(Settings.REMEMBER_ME_DURATION.name, value.inWholeDays.toInt())
}

private enum class Settings {
  DELETE_EMPTY_COLLECTIONS,
  DELETE_EMPTY_READLISTS,
  REMEMBER_ME_KEY,
  REMEMBER_ME_DURATION,
}
