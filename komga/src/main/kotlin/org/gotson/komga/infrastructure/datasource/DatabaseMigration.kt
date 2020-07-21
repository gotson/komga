package org.gotson.komga.infrastructure.datasource

import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.jms.config.JmsListenerEndpointRegistry
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct
import javax.sql.DataSource
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Component
@Profile("!test")
class DatabaseMigration(
  @Qualifier("h2DataSource") private val h2DataSource: DataSource,
  @Qualifier("sqliteDataSource") private val sqliteDataSource: DataSource,
  private val jmsListenerEndpointRegistry: JmsListenerEndpointRegistry,
  @Value("\${spring.datasource.url}") private val h2Url: String,
  private val komgaProperties: KomgaProperties
) {

  // tables in order of creation, to ensure there is no missing foreign key
  private val tables = listOf(
    "LIBRARY",
    "USER",
    "USER_LIBRARY_SHARING",
    "SERIES",
    "SERIES_METADATA",
    "BOOK",
    "MEDIA",
    "MEDIA_PAGE",
    "MEDIA_FILE",
    "BOOK_METADATA",
    "BOOK_METADATA_AUTHOR",
    "READ_PROGRESS",
    "COLLECTION",
    "COLLECTION_SERIES"
  )

  lateinit var h2MigratedFilePath: Path
  lateinit var sqlitePath: Path

  @PostConstruct
  fun init() {
    try {
      logger.info { "Initiating database migration from H2 to SQLite" }

      logger.info { "H2 url: $h2Url" }
      var h2Filename = extractH2Path(h2Url)?.plus(".mv.db")
      if (h2Filename == null) {
        logger.warn { "The H2 URL ($h2Url) does not refer to a file database, skipping migration" }
        return
      }

      val h2Path = convertHomeDir(h2Filename)
      h2Filename = h2Path.toString()
      logger.info { "H2 database file: $h2Filename" }

      if (Files.notExists(h2Path)) {
        logger.warn { "The H2 database file does not exists: $h2Path, skipping migration" }
        return
      }

      h2MigratedFilePath = Paths.get("$h2Filename.migrated")
      if (Files.exists(h2MigratedFilePath)) {
        logger.info { "The H2 database has already been migrated, skipping migration" }
        return
      }

      h2Backup(h2Filename)

      // make sure H2 database is at the latest migration
      flywayMigrateH2()

      sqlitePath = convertHomeDir(komgaProperties.database.file)
      // flyway Migrate must perform exactly one migration (target of one)
      // if it performs 0, the database has already been migrated and probably has data in it
      // it should never perform more than one with a target of 1 migration
      if (flywayMigrateSqlite() != 1)
        throw BeanInitializationException("The SQLite database ($sqlitePath) is not newly minted")

      logger.info { "Stopping all JMS listeners" }
      jmsListenerEndpointRegistry.stop()

      fixH2Database()

      var rows: Int
      measureTime {
        rows = transferH2DataToSqlite()
      }.also {
        val insertsPerSecond = rows / it.inSeconds
        logger.info { "Migration performed in $it ($rows rows). $insertsPerSecond inserts per second." }
      }

      logger.info { "Creating H2 migrated file: $h2MigratedFilePath" }
      Files.createFile(h2MigratedFilePath)

      logger.info { "Starting all JMS listeners" }
      jmsListenerEndpointRegistry.start()

      logger.info { "Migration finished" }

    } catch (e: Exception) {
      logger.error(e) { "Migration failed" }

      if (this::sqlitePath.isInitialized) {
        logger.info { "Deleting Sqlite database if exists" }
        Files.deleteIfExists(sqlitePath)
      }

      if (this::h2MigratedFilePath.isInitialized) {
        logger.info { "Deleting H2 migrated file if exists" }
        Files.deleteIfExists(h2MigratedFilePath)
      }

      throw BeanInitializationException("Migration failed")
    }
  }

  private fun flywayMigrateSqlite(): Int {
    logger.info { "Initialize SQLite database with initial migration: 20200706141854" }
    return Flyway(FluentConfiguration()
      .dataSource(sqliteDataSource)
      .locations("classpath:db/migration/sqlite")
      .target("20200706141854")
    ).migrate()
  }

  private fun flywayMigrateH2(): Int {
    logger.info { "Migrating H2 database to the latest migration" }
    return Flyway(FluentConfiguration()
      .dataSource(h2DataSource)
      .locations("classpath:db/migration/h2")
    ).migrate()
  }

  private fun h2Backup(h2Filename: String) {
    val jdbcTemplate = JdbcTemplate(h2DataSource)
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm-ss"))
    val backup = "$h2Filename.backup.$timestamp.zip"
    logger.info { "Perform a specific backup of the H2 database to: $backup" }
    jdbcTemplate.execute("BACKUP TO '$backup'")
    logger.info { "Backup finished" }
  }

  private fun fixH2Database() {
    logger.info { "Checking H2 database for inconsistent data" }
    val jdbc = JdbcTemplate(h2DataSource)

    val countBook = jdbc.queryForObject("select count(distinct BOOK_ID) from media_page where number is null", Integer::class.java)!!
    if (countBook > 0) {
      logger.info { "Found $countBook books with missing page numbers, marking them as to be re-analyzed" }

      jdbc.update("""
        update media set STATUS='UNKNOWN'
        where BOOK_ID in (
            select distinct BOOK_ID from media_page where number is null
        )""")
      jdbc.update("delete from media_page where number is null")
      jdbc.update("""
        delete from media_page
        where BOOK_ID in (
            select distinct BOOK_ID from media_page where number is null
        )""")
    }

    val invalidReadProgress = jdbc.query("""
      select b.id as BOOK_ID, u.id as USER_ID, count(p.BOOK_ID)
      from read_progress p left join user u on p.user_id = u.id left join book b on p.book_id = b.id
      group by b.id, b.name, u.id, u.email
      having count(p.book_id) > 1
    """) { rs, _ -> Triple(rs.getLong(1), rs.getLong(2), rs.getLong(3)) }

    if (invalidReadProgress.isNotEmpty()) {
      logger.info { "Found ${invalidReadProgress.size} invalid read progress, removing extra rows and keep one per (book,user)" }
      invalidReadProgress.forEach {
        jdbc.update("delete from read_progress where book_id = ? and user_id = ? and rownum() < ?",
          it.first, it.second, it.third
        )
      }
    }
  }

  private fun transferH2DataToSqlite(): Int {
    val maxBatchSize = komgaProperties.database.batchSize

    val sourceConnection = h2DataSource.connection
    val destinationConnection = sqliteDataSource.connection
    var resultSet: ResultSet? = null
    var selectStatement: PreparedStatement? = null
    var insertStatement: PreparedStatement? = null

    var totalRows = 0

    destinationConnection.autoCommit = false
    destinationConnection.transactionIsolation = 1

    try {
      tables.forEach { table ->
        logger.info { "Migrate table: $table" }
        selectStatement = sourceConnection.prepareStatement("select * from $table")
        resultSet = selectStatement!!.executeQuery()
        insertStatement = destinationConnection.prepareStatement(createInsert(resultSet!!.metaData, table))

        var batchSize = 0
        var batchCount = 1
        while (resultSet!!.next()) {
          for (i in 1..resultSet!!.metaData.columnCount) {
            if (resultSet!!.metaData.getColumnType(i) == Types.BLOB) {
              val blob = resultSet!!.getBlob(i)
              val byteArray = blob?.binaryStream?.readBytes()
              insertStatement!!.setObject(i, byteArray)
            } else
              insertStatement!!.setObject(i, resultSet!!.getObject(i))
          }
          insertStatement!!.addBatch()
          batchSize++
          totalRows++

          if (batchSize >= maxBatchSize) {
            insertStatement!!.executeBatch()
            logger.info { "Insert batch #$batchCount ($batchSize rows)" }
            batchSize = 0
            batchCount++
          }
        }
        insertStatement!!.executeBatch()
        logger.info { "Insert batch #$batchCount ($batchSize rows)" }
      }
    } catch (e: Exception) {
      destinationConnection.rollback()
      throw e
    } finally {
      destinationConnection.commit()
      JdbcUtils.closeResultSet(resultSet)
      JdbcUtils.closeStatement(selectStatement)
      JdbcUtils.closeStatement(insertStatement)
      JdbcUtils.closeConnection(sourceConnection)
      JdbcUtils.closeConnection(destinationConnection)
    }

    return totalRows
  }

  private fun createInsert(metadata: ResultSetMetaData, table: String): String {
    val columns = (1..metadata.columnCount).map { metadata.getColumnName(it) }
    val quids = MutableList(columns.size) { "?" }

    return "insert into $table (${columns.joinToString()}) values (${quids.joinToString()})"
  }

}

val excludeH2Url = listOf(":mem:", ":ssl:", ":tcp:", ":zip:")

fun extractH2Path(url: String): String? {
  if (!url.startsWith("jdbc:h2:")) return null
  if (excludeH2Url.any { url.contains(it, ignoreCase = true) }) return null
  return url.split(":").last().split(";").first()
}

fun convertHomeDir(path: String): Path {
  val aPath = Paths.get(path)
  val components = aPath.toList()

  return if (components.first().toString() == "~") {
    Paths.get(System.getProperty("user.home"), *components.drop(1).map { it.toString() }.toTypedArray())
  } else aPath
}
