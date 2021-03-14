package db.migration.sqlite

import mu.KotlinLogging
import org.apache.commons.codec.digest.XXHash32
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class V202101091156000__trash_bin_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val totalCount = jdbcTemplate.queryForObject("select count(*) from BOOK", Long::class.java)
    var currentCount = 0

    logger.info { "Calculating hash for $totalCount books" }
    while (existsRecordWithoutHash(jdbcTemplate)) {
      val values = jdbcTemplate.queryForList("select b.ID, b.URL from BOOK b where b.FILE_HASH == '' limit 500")
      currentCount += values.size

      val params = values.map {
        val hash = getHash(Paths.get((URL(it["URL"] as String).toURI())))
        arrayOf(hash, it["ID"])
      }.toList()
      jdbcTemplate.batchUpdate("UPDATE BOOK SET FILE_HASH = ? WHERE ID = ?", params)
      logger.info { "Hash calculation progress: $currentCount/$totalCount" }
    }
  }

  fun getHash(path: Path): String {
    val file = path.toFile()
    val bytesToSkip = java.lang.Long.highestOneBit(file.length() / 100)

    with(FileInputStream(file)) {
      val hash32 = XXHash32()
      val buf = ByteArray(4096)

      while (true) {
        val read: Int = this.read(buf)
        if (read == -1) break

        hash32.update(buf, 0, read)

        this.skip(bytesToSkip)
      }

      return hash32.value.toString(16)
    }
  }

  private fun existsRecordWithoutHash(jdbcTemplate: JdbcTemplate): Boolean {
    return jdbcTemplate.query("select b.ID from BOOK b where b.FILE_HASH == ''", ResultSetExtractor { it.next() })!!
  }
}
