package db.migration.postgresql

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

class V20200820150923__metadata_fields_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))
    
    // Check if AGE_RATING column exists in BOOK_METADATA table
    val columnExists = try {
      jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'book_metadata' AND column_name = 'age_rating'",
        Int::class.java
      ) ?: 0 > 0
    } catch (e: Exception) {
      false
    }
    
    if (!columnExists) {
      // Column doesn't exist, nothing to migrate
      return
    }

    val bookMetadata = jdbcTemplate.queryForList(
      """select m."AGE_RATING", m."AGE_RATING_LOCK", m."PUBLISHER", m."PUBLISHER_LOCK", m."READING_DIRECTION", m."READING_DIRECTION_LOCK", b."SERIES_ID", m."NUMBER_SORT"
from "BOOK_METADATA" m
left join "BOOK" B on B."ID" = m."BOOK_ID"""",
    )

    if (bookMetadata.isNotEmpty()) {
       val parameters = bookMetadata
        .groupBy { it["series_id"] }
        .map { (seriesId, v) ->
          val ageRating = v.mapNotNull { it["age_rating"] as Int? }.maxOrNull()
          val ageRatingLock = v.mapNotNull { it["age_rating_lock"] as Int? }.maxOrNull()

          val publisher =
            v.filter { (it["publisher"] as String).isNotEmpty() }
              .sortedByDescending { it["number_sort"] as Double? }
              .map { it["publisher"] as String }
              .firstOrNull() ?: ""
          val publisherLock = v.mapNotNull { it["publisher_lock"] as Int? }.maxOrNull()

          val readingDir =
            v.mapNotNull { it["reading_direction"] as String? }
              .groupingBy { it }
              .eachCount()
              .maxByOrNull { it.value }?.key
          val readingDirLock = v.mapNotNull { it["reading_direction_lock"] as Int? }.maxOrNull()

          arrayOf(ageRating, ageRatingLock, publisher, publisherLock, readingDir, readingDirLock, seriesId)
        }

       jdbcTemplate.batchUpdate(
        """UPDATE "SERIES_METADATA" SET "AGE_RATING" = ?, "AGE_RATING_LOCK" = ?, "PUBLISHER" = ?, "PUBLISHER_LOCK" = ?, "READING_DIRECTION" = ?, "READING_DIRECTION_LOCK" = ? WHERE "SERIES_ID" = ?""",
        parameters,
      )
    }
  }
}
