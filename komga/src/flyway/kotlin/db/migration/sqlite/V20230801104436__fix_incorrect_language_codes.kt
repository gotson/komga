package db.migration.sqlite

import com.ibm.icu.util.ULocale
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

private val logger = KotlinLogging.logger {}

class V20230801104436__fix_incorrect_language_codes : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val seriesLanguage = jdbcTemplate.queryForList(
      """select m.SERIES_ID, m.LANGUAGE from SERIES_METADATA m where LANGUAGE <> '' and LANGUAGE <> 'en'""",
    )

    if (seriesLanguage.isNotEmpty()) {
      seriesLanguage.mapNotNull {
        val language = it["LANGUAGE"].toString()
        if (language.isBlank()) null
        else {
          val languageNormalized = normalize(language)
          if (language == languageNormalized) null
          else arrayOf(languageNormalized, it["SERIES_ID"])
        }
      }.let { params ->
        logger.info { "Updating ${params.size} incorrect language codes for Series metadata" }
        jdbcTemplate.batchUpdate(
          """update SERIES_METADATA set LANGUAGE = ? where SERIES_ID = ?""",
          params,
        )
      }
    }
  }

  private fun normalize(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return try {
      ULocale.forLanguageTag(value).toLanguageTag()
    } catch (e: Exception) {
      ""
    }
  }
}
