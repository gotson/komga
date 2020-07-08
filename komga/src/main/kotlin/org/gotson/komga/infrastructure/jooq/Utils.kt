package org.gotson.komga.infrastructure.jooq

import org.jooq.Field
import org.jooq.SortField
import org.springframework.data.domain.Sort
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
