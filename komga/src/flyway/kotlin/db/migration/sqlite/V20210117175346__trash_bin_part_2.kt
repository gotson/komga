package db.migration.sqlite

import mu.KotlinLogging
import net.jpountz.xxhash.XXHashFactory
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class V202101091156000__trash_bin_part_2 : BaseJavaMigration() {
  override fun migrate(context: Context) {
    val jdbcTemplate = NamedParameterJdbcTemplate(SingleConnectionDataSource(context.connection, true))

    val totalCount = jdbcTemplate.queryForObject("select count(*) from BOOK", mapOf<String, Any>(), Long::class.java)
    var currentCount = 0

    logger.info { "calculating hash for $totalCount books" }
    while (existsRecordWithoutHash(jdbcTemplate.jdbcTemplate)) {
      val values = jdbcTemplate.queryForList("select b.ID, b.URL from BOOK b where b.FILE_HASH == '' limit 500", mapOf<String, Any>())
      currentCount += values.size

      val params: List<MapSqlParameterSource> = values.map {
        val path = Paths.get((URL(it["URL"] as String?).toURI()))

        MapSqlParameterSource()
          .addValue("id", it["ID"])
          .addValue("file_hash", getHash(path))
      }.toList()
      jdbcTemplate.batchUpdate("UPDATE BOOK SET FILE_HASH = :file_hash WHERE ID = :id", params.toTypedArray())
      logger.info { "hash calculation progress: $currentCount/$totalCount" }
    }
  }

  fun getHash(path: Path): String {
    val seed = -0x68b84d74L
    val file = path.toFile()
    val bytesToSkip = java.lang.Long.highestOneBit(file.length() / 100)

    with(FileInputStream(file)) {

      val buf = ByteArray(4096)
      val hash64 = XXHashFactory.fastestInstance().newStreamingHash64(seed)

      while (true) {
        val read: Int = this.read(buf)
        if (read == -1) {
          break
        }

        hash64.update(buf, 0, read)
        this.skip(bytesToSkip)
      }

      return hash64.value.toString(16)
    }
  }

  private fun existsRecordWithoutHash(jdbcTemplate: JdbcTemplate): Boolean {
    return jdbcTemplate.query("select b.ID from BOOK b where b.FILE_HASH == ''", ResultSetExtractor { it.next() })!!
  }
}
