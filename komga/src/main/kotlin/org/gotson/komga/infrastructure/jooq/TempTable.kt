package org.gotson.komga.infrastructure.jooq

import com.github.f4b6a3.tsid.TsidCreator
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.io.Closeable

/**
 * Temporary table with a single STRING column.
 * This is made to store collection of values that are too long to be specified in a query condition,
 * by using a sub-select instead.
 *
 * The table name is automatically generated, and the table is dropped when the object is closed.
 */
class TempTable private constructor(
  private val dslContext: DSLContext,
  val name: String,
) : Closeable {
  constructor(dslContext: DSLContext) : this(dslContext, generateName())

  private var created = false

  fun create() {
    dslContext.execute("CREATE TEMPORARY TABLE $name (STRING varchar NOT NULL);")
    created = true
  }

  fun insertTempStrings(
    batchSize: Int,
    collection: Collection<String>,
  ) {
    if (!created) create()
    if (collection.isNotEmpty()) {
      collection.chunked(batchSize).forEach { chunk ->
        dslContext
          .batch(
            dslContext.insertInto(DSL.table(DSL.name(name)), DSL.field(DSL.name("STRING"), String::class.java)).values(null as String?),
          ).also { step ->
            chunk.forEach {
              step.bind(it)
            }
          }.execute()
      }
    }
  }

  fun selectTempStrings() = dslContext.select(DSL.field(DSL.name("STRING"), String::class.java)).from(DSL.table(DSL.name(name)))

  override fun close() {
    if (created) dslContext.dropTableIfExists(name).execute()
  }

  companion object {
    private fun generateName() = "temp_${TsidCreator.getTsid256()}"

    fun DSLContext.withTempTable(
      batchSize: Int,
      collection: Collection<String>,
    ) = TempTable(this, generateName())
      .also {
        it.insertTempStrings(batchSize, collection)
      }
  }
}
