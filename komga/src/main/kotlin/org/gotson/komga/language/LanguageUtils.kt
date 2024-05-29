package org.gotson.komga.language

import org.apache.commons.lang3.StringUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.Date
import java.util.Enumeration
import java.util.SortedMap

fun <T> List<T>.toIndexedMap(): SortedMap<Int, T> =
  mapIndexed { i, e -> i to e }.toMap().toSortedMap()

fun <T> List<T>.toEnumeration(): Enumeration<T> {
  return object : Enumeration<T> {
    var count = 0

    override fun hasMoreElements(): Boolean {
      return this.count < size
    }

    override fun nextElement(): T {
      if (this.count < size) {
        return get(this.count++)
      }
      throw NoSuchElementException("List enumeration asked for more elements than present")
    }
  }
}

fun <T, R : Any> Iterable<T>.mostFrequent(transform: (T) -> R?): R? {
  return this
    .mapNotNull(transform)
    .groupingBy { it }
    .eachCount()
    .maxByOrNull { it.value }?.key
}

fun Iterable<String>.lowerNotBlank() =
  this.map { it.lowercase().trim() }.filter { it.isNotBlank() }

fun <T> Iterable<T>.toSetOrNull() =
  this.toSet().ifEmpty { null }

fun LocalDateTime.notEquals(
  other: LocalDateTime,
  precision: TemporalUnit = ChronoUnit.MILLIS,
) =
  this.truncatedTo(precision) != other.truncatedTo(precision)

fun String.stripAccents(): String = StringUtils.stripAccents(this)

fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.of("Z")).toInstant())

/**
 * Warning: this is not idempotent
 */
fun LocalDateTime.toUTC(): LocalDateTime =
  atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

fun LocalDateTime.toUTCZoned(): ZonedDateTime =
  atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)

fun LocalDateTime.toZonedDateTime(): ZonedDateTime =
  this.atZone(ZoneId.of("Z")).withZoneSameInstant(ZoneId.systemDefault())

fun LocalDateTime.toCurrentTimeZone(): LocalDateTime =
  this.atZone(ZoneId.of("Z")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

fun Iterable<String>.contains(
  s: String,
  ignoreCase: Boolean = false,
): Boolean =
  any { it.equals(s, ignoreCase) }
