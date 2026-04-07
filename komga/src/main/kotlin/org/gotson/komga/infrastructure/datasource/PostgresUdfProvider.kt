package org.gotson.komga.infrastructure.datasource

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Field
import org.jooq.impl.DSL
import java.sql.Connection

private val log = KotlinLogging.logger {}

class PostgresUdfProvider : DatabaseUdfProvider {
  override val udfStripAccentsName = "UDF_STRIP_ACCENTS"
  override val collationUnicode3Name = "COLLATION_UNICODE_3"

  override fun Field<String>.udfStripAccents(): Field<String> =
    // PostgreSQL has unaccent extension, but we'll implement it in application layer
    // For now, we'll create a placeholder function
    DSL.function(udfStripAccentsName, String::class.java, this)

  override fun Field<String>.collateUnicode3(): Field<String> =
    // PostgreSQL uses ICU collations, we'll use "und-u-ks-level2" for Unicode collation
    this.collate("und-u-ks-level2")

  override fun initializeConnection(connection: Any) {
    val pgConnection = connection as Connection
    log.debug { "Initializing PostgreSQL connection with custom functions" }

    // Create the strip accents function if it doesn't exist
    val createFunctionSQL =
      """
      CREATE OR REPLACE FUNCTION $udfStripAccentsName(text TEXT)
      RETURNS TEXT AS $$
      BEGIN
          -- This is a placeholder. In production, you might want to:
          -- 1. Use the unaccent extension: SELECT unaccent(text)
          -- 2. Or implement custom logic in application layer
          RETURN text;
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
