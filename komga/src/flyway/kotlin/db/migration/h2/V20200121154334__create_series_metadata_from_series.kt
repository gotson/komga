package db.migration.h2

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class V20200121154334__create_series_metadata_from_series : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val seriesIds = jdbcTemplate.queryForList("SELECT id FROM series", Long::class.java)

    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))
    seriesIds.forEach { seriesId ->
      val metadataId = jdbcTemplate.queryForObject("SELECT NEXTVAL('hibernate_sequence')", Int::class.java)
      jdbcTemplate.execute("INSERT INTO series_metadata (ID, CREATED_DATE, LAST_MODIFIED_DATE, STATUS) VALUES ($metadataId, '$now', '$now', 'ONGOING')")
      jdbcTemplate.execute("UPDATE series SET metadata_id = $metadataId WHERE id = $seriesId")
    }
  }
}
