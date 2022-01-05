package db.migration.sqlite

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

class V20200820150923__metadata_fields_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val bookMetadata = jdbcTemplate.queryForList(
      """select m.AGE_RATING, m.AGE_RATING_LOCK, m.PUBLISHER, m.PUBLISHER_LOCK, m.READING_DIRECTION, m.READING_DIRECTION_LOCK, b.SERIES_ID, m.NUMBER_SORT
from BOOK_METADATA m
left join BOOK B on B.ID = m.BOOK_ID""",
    )

    if (bookMetadata.isNotEmpty()) {
      val parameters = bookMetadata
        .groupBy { it["SERIES_ID"] }
        .map { (seriesId, v) ->
          val ageRating = v.mapNotNull { it["AGE_RATING"] as Int? }.maxOrNull()
          val ageRatingLock = v.mapNotNull { it["AGE_RATING_LOCK"] as Int? }.maxOrNull()

          val publisher =
            v.filter { (it["PUBLISHER"] as String).isNotEmpty() }
              .sortedByDescending { it["NUMBER_SORT"] as Double? }
              .map { it["PUBLISHER"] as String }
              .firstOrNull() ?: ""
          val publisherLock = v.mapNotNull { it["PUBLISHER_LOCK"] as Int? }.maxOrNull()

          val readingDir =
            v.mapNotNull { it["READING_DIRECTION"] as String? }
              .groupingBy { it }
              .eachCount()
              .maxByOrNull { it.value }?.key
          val readingDirLock = v.mapNotNull { it["READING_DIRECTION_LOCK"] as Int? }.maxOrNull()

          arrayOf(ageRating, ageRatingLock, publisher, publisherLock, readingDir, readingDirLock, seriesId)
        }

      jdbcTemplate.batchUpdate(
        "UPDATE SERIES_METADATA SET AGE_RATING = ?, AGE_RATING_LOCK = ?, PUBLISHER = ?, PUBLISHER_LOCK = ?, READING_DIRECTION = ?, READING_DIRECTION_LOCK = ? WHERE SERIES_ID = ?",
        parameters,
      )
    }
  }
}
