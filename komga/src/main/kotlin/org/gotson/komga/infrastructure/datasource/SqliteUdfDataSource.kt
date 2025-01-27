package org.gotson.komga.infrastructure.datasource

import com.ibm.icu.text.Collator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.language.stripAccents
import org.sqlite.Collation
import org.sqlite.Function
import org.sqlite.SQLiteConnection
import org.sqlite.SQLiteDataSource
import java.sql.Connection

private val log = KotlinLogging.logger {}

class SqliteUdfDataSource : SQLiteDataSource() {
  companion object {
    const val UDF_STRIP_ACCENTS = "UDF_STRIP_ACCENTS"
    const val COLLATION_UNICODE_3 = "COLLATION_UNICODE_3"
  }

  override fun getConnection(): Connection = super.getConnection().also { addAllUdf(it as SQLiteConnection) }

  override fun getConnection(
    username: String?,
    password: String?,
  ): SQLiteConnection = super.getConnection(username, password).also { addAllUdf(it) }

  private fun addAllUdf(connection: SQLiteConnection) {
    createUdfRegexp(connection)
    createUdfStripAccents(connection)
    createUnicode3Collation(connection)
  }

  private fun createUdfRegexp(connection: SQLiteConnection) {
    log.debug { "Adding custom REGEXP function" }
    Function.create(
      connection,
      "REGEXP",
      object : Function() {
        override fun xFunc() {
          val regexp = (value_text(0) ?: "").toRegex(RegexOption.IGNORE_CASE)
          val text = value_text(1) ?: ""

          result(if (regexp.containsMatchIn(text)) 1 else 0)
        }
      },
    )
  }

  private fun createUdfStripAccents(connection: SQLiteConnection) {
    log.debug { "Adding custom $UDF_STRIP_ACCENTS function" }
    Function.create(
      connection,
      UDF_STRIP_ACCENTS,
      object : Function() {
        override fun xFunc() =
          when (val text = value_text(0)) {
            null -> error("Argument must not be null")
            else -> result(text.stripAccents())
          }
      },
    )
  }

  private fun createUnicode3Collation(connection: SQLiteConnection) {
    log.debug { "Adding custom $COLLATION_UNICODE_3 collation" }
    Collation.create(
      connection,
      COLLATION_UNICODE_3,
      object : Collation() {
        val collator =
          Collator.getInstance().apply {
            strength = Collator.TERTIARY
            decomposition = Collator.CANONICAL_DECOMPOSITION
          }

        override fun xCompare(
          str1: String,
          str2: String,
        ): Int = collator.compare(str1, str2)
      },
    )
  }
}
