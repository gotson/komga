package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.infrastructure.datasource.DatabaseUdfProvider
import org.jooq.Condition
import org.jooq.Field
import org.springframework.stereotype.Component

@Component
class JooqUdfHelper(
  private val databaseUdfProvider: DatabaseUdfProvider,
) {
  fun Field<String>.udfStripAccents(): Field<String> = databaseUdfProvider.run { this@udfStripAccents.udfStripAccents() }

  fun Field<String>.collateUnicode3(): Field<String> = databaseUdfProvider.run { this@collateUnicode3.collateUnicode3() }

  fun regexp(field: Field<String>, pattern: String, caseSensitive: Boolean = false): Condition =
    databaseUdfProvider.regexp(field, pattern, caseSensitive)
}
