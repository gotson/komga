package db.migration.postgresql

import com.github.f4b6a3.tsid.TsidCreator
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

// This migration will copy the existing thumbnails in MEDIA to the new table THUMBNAIL_BOOK,
// adding a generated TSID as the ID
// Modified to check if THUMBNAIL column exists before attempting migration
class V20200810154730__thumbnails_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))
    
    // Check if THUMBNAIL column exists in MEDIA table
    // Note: information_schema stores identifiers in lowercase unless quoted
    val columnExists = try {
      jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'MEDIA' AND column_name = 'THUMBNAIL'",
        Int::class.java
      ) ?: 0 > 0
    } catch (e: Exception) {
      false
    }
    
    if (!columnExists) {
      // THUMBNAIL column doesn't exist, nothing to migrate
      return
    }

    val thumbnails = jdbcTemplate.queryForList("SELECT THUMBNAIL, BOOK_ID FROM MEDIA")

    if (thumbnails.isNotEmpty()) {
      val parameters = thumbnails.map {
        arrayOf(TsidCreator.getTsid256().toString(), it["THUMBNAIL"], it["BOOK_ID"])
      }

      jdbcTemplate.batchUpdate(
        "INSERT INTO THUMBNAIL_BOOK(ID, THUMBNAIL, SELECTED, TYPE, BOOK_ID) values (?, ?, 1, 'GENERATED', ?)",
        parameters,
      )
    }
  }
}
