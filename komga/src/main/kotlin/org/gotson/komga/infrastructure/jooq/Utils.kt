package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.jooq.Tables
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SortField
import org.jooq.impl.DSL
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun Field<String>.noCase() = this.collate("NOCASE")

fun LocalDateTime.toUTC(): LocalDateTime =
  atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

fun Sort.toOrderBy(sorts: Map<String, Field<out Any>>): List<SortField<out Any>> =
  this.mapNotNull {
    it.toSortField(sorts)
  }

fun Sort.Order.toSortField(sorts: Map<String, Field<out Any>>): SortField<out Any>? {
  val f = sorts[property] ?: return null
  return if (isAscending) f.asc() else f.desc()
}

fun Field<String>.sortByValues(values: List<String>, asc: Boolean = true): Field<Int> {
  var c = DSL.choose(this).`when`("dummy dsl", Int.MAX_VALUE)
  val multiplier = if (asc) 1 else -1
  values.forEachIndexed { index, value -> c = c.`when`(value, index * multiplier) }
  return c.otherwise(Int.MAX_VALUE)
}

fun Field<String>.inOrNoCondition(list: List<String>?): Condition =
  when {
    list == null -> DSL.noCondition()
    list.isEmpty() -> DSL.falseCondition()
    else -> this.`in`(list)
  }

fun LocalDateTime.toCurrentTimeZone(): LocalDateTime =
  this.atZone(ZoneId.of("Z")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

fun Field<String>.udfStripAccents() =
  DSL.function(SqliteUdfDataSource.udfStripAccents, String::class.java, this)

fun DSLContext.insertTempStrings(batchSize: Int, collection: Collection<String>) {
  this.deleteFrom(Tables.TEMP_STRING_LIST).execute()
  if (collection.isNotEmpty()) {
    collection.chunked(batchSize).forEach { chunk ->
      this.batch(
        this.insertInto(Tables.TEMP_STRING_LIST, Tables.TEMP_STRING_LIST.STRING).values(null as String?)
      ).also { step ->
        chunk.forEach {
          step.bind(it)
        }
      }.execute()
    }
  }
}

fun DSLContext.selectTempStrings() = this.select(Tables.TEMP_STRING_LIST.STRING).from(Tables.TEMP_STRING_LIST)
