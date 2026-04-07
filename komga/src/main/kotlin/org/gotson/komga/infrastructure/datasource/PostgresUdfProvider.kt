package org.gotson.komga.infrastructure.datasource

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL
import java.sql.Connection

private val log = KotlinLogging.logger {}

class PostgresUdfProvider : DatabaseUdfProvider {
  override val udfStripAccentsName = "UDF_STRIP_ACCENTS"
  override val collationUnicode3Name = "COLLATION_UNICODE_3"

  override fun Field<String>.udfStripAccents(): Field<String> =
    // Use PostgreSQL's unaccent extension
    DSL.function("unaccent", String::class.java, this)

  override fun Field<String>.collateUnicode3(): Field<String> =
    // PostgreSQL uses ICU collations, we'll use "und-u-ks-level2" for Unicode collation
    // which provides case-insensitive, accent-insensitive sorting
    this.collate("und-u-ks-level2")

  override fun regexp(field: Field<String>, pattern: String, caseSensitive: Boolean): Condition {
    // PostgreSQL uses ~ for regex matching, ~* for case-insensitive
    return if (caseSensitive) {
      DSL.condition("{0} ~ {1}", field, DSL.inline(pattern))
    } else {
      DSL.condition("{0} ~* {1}", field, DSL.inline(pattern))
    }
  }

  override fun initializeConnection(connection: Any) {
    val pgConnection = connection as Connection
    log.debug { "Initializing PostgreSQL connection with custom functions" }

    // Ensure unaccent extension is available
    try {
      val checkExtensionSQL = "SELECT extname FROM pg_extension WHERE extname = 'unaccent'"
      val rs = pgConnection.createStatement().executeQuery(checkExtensionSQL)
      if (!rs.next()) {
        log.warn { "unaccent extension not found. Attempting to create it..." }
        pgConnection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS unaccent")
        log.info { "Created unaccent extension" }
      } else {
        log.debug { "unaccent extension already exists" }
      }
    } catch (e: Exception) {
      log.error(e) { "Failed to check/create unaccent extension" }
    }

    // Create a wrapper function for UDF_STRIP_ACCENTS that uses unaccent
    val createFunctionSQL =
      """
      CREATE OR REPLACE FUNCTION $udfStripAccentsName(text TEXT)
      RETURNS TEXT AS $$
      BEGIN
          RETURN unaccent(text);
      END;
      $$ LANGUAGE plpgsql IMMUTABLE;
      """.trimIndent()

    try {
      pgConnection.createStatement().execute(createFunctionSQL)
      log.debug { "Created PostgreSQL function $udfStripAccentsName" }
    } catch (e: Exception) {
      log.error(e) { "Failed to create PostgreSQL function $udfStripAccentsName" }
    }
  }
}
