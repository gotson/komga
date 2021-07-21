package org.gotson.komga.infrastructure.datasource

import mu.KotlinLogging
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.sqlite.Function
import org.sqlite.SQLiteConnection
import java.sql.Connection

private val log = KotlinLogging.logger {}

class SqliteUdfDataSource : SimpleDriverDataSource() {

  override fun getConnection(): Connection =
    super.getConnection().also { createUdfRegexp(it as SQLiteConnection) }

  override fun getConnection(username: String, password: String): Connection =
    super.getConnection(username, password).also { createUdfRegexp(it as SQLiteConnection) }

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
}
