package org.gotson.komga.infrastructure.datasource

object DatabaseCompatibility {
  // These constants are maintained for backward compatibility
  // They will be dynamically resolved based on the active database type

  @Deprecated("Use DatabaseUdfProvider instead", ReplaceWith("databaseUdfProvider.udfStripAccentsName"))
  const val UDF_STRIP_ACCENTS = "UDF_STRIP_ACCENTS"

  @Deprecated("Use DatabaseUdfProvider instead", ReplaceWith("databaseUdfProvider.collationUnicode3Name"))
  const val COLLATION_UNICODE_3 = "COLLATION_UNICODE_3"
}
