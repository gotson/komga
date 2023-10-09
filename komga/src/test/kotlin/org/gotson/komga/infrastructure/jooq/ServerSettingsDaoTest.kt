package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ServerSettingsDaoTest(
  @Autowired private val serverSettingsDao: ServerSettingsDao,
) {
  @AfterEach
  fun cleanup() {
    serverSettingsDao.deleteAll()
  }

  @Test
  fun `when saving String setting then it is persisted`() {
    serverSettingsDao.saveSetting("setting", "value")

    val fetch = serverSettingsDao.getSettingByKey("setting", String::class.java)

    assertThat(fetch).isEqualTo("value")
  }

  @Test
  fun `when saving Int setting then it is persisted`() {
    serverSettingsDao.saveSetting("setting", 12)

    val fetch = serverSettingsDao.getSettingByKey("setting", Int::class.java)

    assertThat(fetch).isEqualTo(12)
  }

  @Test
  fun `when saving Boolean setting then it is persisted`() {
    serverSettingsDao.saveSetting("setting", true)

    val fetch = serverSettingsDao.getSettingByKey("setting", Boolean::class.java)

    assertThat(fetch).isTrue
  }

  @Test
  fun `given existing setting when saving again then it is overriden`() {
    serverSettingsDao.saveSetting("setting", "value")

    val initial = serverSettingsDao.getSettingByKey("setting", String::class.java)
    assertThat(initial).isEqualTo("value")

    serverSettingsDao.saveSetting("setting", "updated")
    val updated = serverSettingsDao.getSettingByKey("setting", String::class.java)
    assertThat(updated).isEqualTo("updated")
  }
}
