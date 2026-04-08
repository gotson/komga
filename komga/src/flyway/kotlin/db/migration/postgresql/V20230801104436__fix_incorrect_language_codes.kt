package db.migration.postgresql

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
    
    // Check if LANGUAGE column exists in SERIES_METADATA table
    val columnExists = try {
      jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'series_metadata' AND column_name = 'language'",
        Int::class.java
      ) ?: 0 > 0
    } catch (e: Exception) {
      false
    }
    
    if (!columnExists) {
      // Column doesn't exist, nothing to migrate
      return
    }

    val seriesLanguage = jdbcTemplate.queryForList(
      """select m."SERIES_ID", m."LANGUAGE" from "SERIES_METADATA" m where "LANGUAGE" <> '' and "LANGUAGE" <> 'en'""",
    )

    if (seriesLanguage.isNotEmpty()) {
      seriesLanguage.mapNotNull {
          val language = it["language"].toString()
        if (language.isBlank()) null
        else {
          val languageNormalized = normalize(language)
          if (language == languageNormalized) null
          else arrayOf(languageNormalized, it["series_id"])
        }
      }.let { params ->
        logger.info { "Updating ${params.size} incorrect language codes for Series metadata" }
        jdbcTemplate.batchUpdate(
          """update "SERIES_METADATA" set "LANGUAGE" = ? where "SERIES_ID" = ?""",
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
