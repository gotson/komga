package org.gotson.komga.infrastructure.datasource

import com.ibm.icu.text.Collator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.language.stripAccents
import org.jooq.Field
import org.jooq.impl.DSL
import org.sqlite.Collation
import org.sqlite.Function
import org.sqlite.SQLiteConnection
import java.sql.Connection

private val log = KotlinLogging.logger {}

class SqliteUdfProvider : DatabaseUdfProvider {
  override val udfStripAccentsName = "UDF_STRIP_ACCENTS"
  override val collationUnicode3Name = "COLLATION_UNICODE_3"

  override fun Field<String>.udfStripAccents(): Field<String> = DSL.function(udfStripAccentsName, String::class.java, this)

  override fun Field<String>.collateUnicode3(): Field<String> = this.collate(collationUnicode3Name)

  override fun initializeConnection(connection: Any) {
    val sqliteConnection = connection as SQLiteConnection
    createUdfRegexp(sqliteConnection)
    createUdfStripAccents(sqliteConnection)
    createUnicode3Collation(sqliteConnection)
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
    log.debug { "Adding custom $udfStripAccentsName function" }
    Function.create(
      connection,
      udfStripAccentsName,
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
    log.debug { "Adding custom $collationUnicode3Name collation" }
    Collation.create(
      connection,
      collationUnicode3Name,
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
