package db.migration.h2

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.net.URI
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class V20190926114415__create_library_from_series : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val urls = jdbcTemplate.queryForList("SELECT url FROM serie", String::class.java)

    if (urls.isNotEmpty()) {
      val rootFolder = findCommonDirPath(urls, '/')

      val libraryId = jdbcTemplate.queryForObject("SELECT NEXTVAL('hibernate_sequence')", Int::class.java)
      val libraryName = Paths.get(URI(rootFolder)).fileName

      val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))

      jdbcTemplate.execute("INSERT INTO library (ID, CREATED_DATE, LAST_MODIFIED_DATE, NAME, ROOT) VALUES ($libraryId, '$now', '$now', '$libraryName',  '$rootFolder')")
      jdbcTemplate.execute("UPDATE serie SET library_id = $libraryId")
    }
  }
}

// version 1.1.51 - https://www.rosettacode.org/wiki/Find_common_directory_path#Kotlin
fun findCommonDirPath(paths: List<String>, separator: Char): String {
  if (paths.isEmpty()) return ""
  if (paths.size == 1) return paths[0]
  val splits = paths[0].split(separator)
  val n = splits.size
  val paths2 = paths.drop(1)
  var k = 0
  var common = ""
  while (true) {
    val prevCommon = common
    common += if (k == 0) splits[0] else separator + splits[k]
    if (!paths2.all { it.startsWith(common + separator) || it == common }) return prevCommon
    if (++k == n) return common
  }
}
