package org.gotson.komga.infrastructure.datasource

import mu.KotlinLogging
import org.gotson.komga.infrastructure.language.stripAccents
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.sqlite.Function
import org.sqlite.SQLiteConnection
import java.sql.Connection

private val log = KotlinLogging.logger {}

class SqliteUdfDataSource : SimpleDriverDataSource() {

  companion object {
    const val udfStripAccents = "UDF_STRIP_ACCENTS"
  }

  override fun getConnection(): Connection =
    super.getConnection().also { addAllUdf(it as SQLiteConnection) }

  override fun getConnection(username: String, password: String): Connection =
    super.getConnection(username, password).also { addAllUdf(it as SQLiteConnection) }

  private fun addAllUdf(connection: SQLiteConnection) {
    createUdfRegexp(connection)
    createUdfStripAccents(connection)
  }

  private fun createUdfRegexp(connection: SQLiteConnection) {
    log.debug { "Adding custom REGEXP function" }
    Function.create(
      connection, "REGEXP",
      object : Function() {
        override fun xFunc() {
          val regexp = (value_text(0) ?: "").toRegex(RegexOption.IGNORE_CASE)
          val text = value_text(1) ?: ""

          result(if (regexp.containsMatchIn(text)) 1 else 0)
        }
      }
    )
  }

  private fun createUdfStripAccents(connection: SQLiteConnection) {
    log.debug { "Adding custom $udfStripAccents function" }
    Function.create(
      connection, udfStripAccents,
      object : Function() {
        override fun xFunc() =
          when (val text = value_text(0)) {
            null -> error("Argument must not be null")
            else -> result(text.stripAccents())
          }
      }
    )
  }
}
