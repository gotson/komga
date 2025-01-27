package db.migration.sqlite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private val logger = KotlinLogging.logger {}

class V20240422132621__fix_read_progress_locators : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val readProgressList = jdbcTemplate.queryForList(
      """select r.BOOK_ID, r.USER_ID, r.locator from READ_PROGRESS r where locator is not null""",
    )

    if (readProgressList.isNotEmpty()) {
      val mapper = ObjectMapper()

      readProgressList.mapNotNull {
        try {
          val locator = GZIPInputStream((it["LOCATOR"] as ByteArray).inputStream()).use { gz -> mapper.readTree(gz) }
          val href = locator["href"]?.asText()
          if (href == null) null
          else {
            val correctHref = href.replaceBefore("/resource/", "").removePrefix("/resource/")
            (locator as ObjectNode).replace("href", TextNode(correctHref))
            val gzLocator = ByteArrayOutputStream().use { baos ->
              GZIPOutputStream(baos).use { gz ->
                mapper.writeValue(gz, locator)
                baos.toByteArray()
              }
            }
            arrayOf(gzLocator, it["BOOK_ID"], it["USER_ID"])
          }
        } catch (e: Exception) {
          null
        }
      }.let { params ->
        logger.info { "Updating ${params.size} incorrect read progress locators" }
        jdbcTemplate.batchUpdate(
          """update READ_PROGRESS set locator = ? where BOOK_ID = ? and USER_ID = ?""",
          params,
        )
      }
    }
  }
}
