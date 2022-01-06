package db.migration.sqlite

import com.github.f4b6a3.tsid.TsidCreator
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

// This migration will copy the existing thumbnails in MEDIA to the new table THUMBNAIL_BOOK,
// adding a generated TSID as the ID
class V20200810154730__thumbnails_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

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
