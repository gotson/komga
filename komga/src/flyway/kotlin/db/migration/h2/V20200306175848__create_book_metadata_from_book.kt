package db.migration.h2

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class V20200306175848__create_book_metadata_from_book : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val books = jdbcTemplate.queryForList("SELECT id, name, number FROM book")

    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))
    books.forEach { book ->
      val metadataId = jdbcTemplate.queryForObject("SELECT NEXTVAL('hibernate_sequence')", Int::class.java)
      jdbcTemplate.update(
        "INSERT INTO book_metadata (ID, CREATED_DATE, LAST_MODIFIED_DATE, TITLE, NUMBER, NUMBER_SORT) VALUES (?, ?, ?, ?, ?, ?)",
        metadataId,
        now,
        now,
        book["name"],
        book["number"],
        book["number"]
      )
      jdbcTemplate.update(
        "UPDATE book SET metadata_id = ? WHERE id = ?",
        metadataId,
        book["id"]
      )
    }

    jdbcTemplate.execute("alter table book alter column metadata_id set not null")
  }
}
