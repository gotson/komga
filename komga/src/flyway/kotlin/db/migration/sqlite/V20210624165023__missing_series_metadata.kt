package db.migration.sqlite

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

private val logger = KotlinLogging.logger {}

class V20210624165023__missing_series_metadata : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val seriesWithoutMetada = jdbcTemplate.queryForList(
      """select s.ID, s.NAME from SERIES s where s.ID not in (select sm.SERIES_ID from SERIES_METADATA sm)""",
    )

    if (seriesWithoutMetada.isNotEmpty()) {
      logger.info { "Found ${seriesWithoutMetada.size} series without metadata" }

      seriesWithoutMetada
        .map {
          // fields for SERIES_METADATA: SERIES_ID, STATUS=ONGOING, TITLE, TITLE_SORT, READING_DIRECTION=null, AGE_RATING=null
          arrayOf(it["ID"], "ONGOING", it["NAME"], StringUtils.stripAccents(it["NAME"].toString()), null, null)
        }.let { parameters ->
          jdbcTemplate.batchUpdate(
            "INSERT INTO SERIES_METADATA(SERIES_ID, STATUS, TITLE, TITLE_SORT, READING_DIRECTION, AGE_RATING) VALUES (?,?,?,?,?,?)",
            parameters,
          )
        }

      seriesWithoutMetada
        .map {
          // fields for BOOK_METADATA_AGGREGATION: SERIES_ID, RELEASE_DATE=null
          arrayOf(it["ID"], null)
        }.let { parameters ->
          jdbcTemplate.batchUpdate(
            "INSERT INTO BOOK_METADATA_AGGREGATION(SERIES_ID, RELEASE_DATE) VALUES (?,?)",
            parameters,
          )
        }
    }
  }
}
