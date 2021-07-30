package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.jooq.Condition
import org.jooq.Field
import org.jooq.SortField
import org.jooq.Table
import org.jooq.impl.DSL
import org.springframework.data.domain.Sort
import org.sqlite.SQLiteException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.toUTC(): LocalDateTime =
  atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

fun Sort.toOrderBy(sorts: Map<String, Field<out Any>>): List<SortField<out Any>> =
  this.mapNotNull {
    val f = sorts[it.property]
    if (it.isAscending) f?.asc() else f?.desc()
  }

fun LocalDateTime.toCurrentTimeZone(): LocalDateTime =
  this.atZone(ZoneId.of("Z")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

fun Field<String>.udfStripAccents() =
  DSL.function(SqliteUdfDataSource.udfStripAccents, String::class.java, this)

fun Table<*>.match(term: String): Condition =
  DSL.condition("{0} MATCH {1}", DSL.field(this.name), term.ftsSanitized())

fun String.ftsSanitized() = this
  .replace("-", " ") // to better match queries like "x-men"
  .replace("[^\\p{L}\\p{Z}\\p{N}\":+*^{}()]".toRegex(), "") // to avoid fts5 syntax error
  .removePrefix("*") // to avoid unknown special query

private val ftsErrorMessages = listOf("no such column", "unknown special query", "fts5: syntax error near", "unterminated string")

/**
 * FTS queries of the form field:term with a field name that doesn't exist will raise an exception
 * given the same search string can be requested for different object type, this could happen quite often
 */
fun Exception.isFtsError() =
  cause is SQLiteException &&
    ftsErrorMessages.any { message?.contains(it) == true }
