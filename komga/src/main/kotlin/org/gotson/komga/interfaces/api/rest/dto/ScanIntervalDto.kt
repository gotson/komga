package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.Library

enum class ScanIntervalDto {
  DISABLED,
  HOURLY,
  EVERY_6H,
  EVERY_12H,
  DAILY,
  WEEKLY,
}

fun Library.ScanInterval.toDto() =
  when (this) {
    Library.ScanInterval.DISABLED -> ScanIntervalDto.DISABLED
    Library.ScanInterval.HOURLY -> ScanIntervalDto.HOURLY
    Library.ScanInterval.EVERY_6H -> ScanIntervalDto.EVERY_6H
    Library.ScanInterval.EVERY_12H -> ScanIntervalDto.EVERY_12H
    Library.ScanInterval.DAILY -> ScanIntervalDto.DAILY
    Library.ScanInterval.WEEKLY -> ScanIntervalDto.WEEKLY
  }

fun ScanIntervalDto.toDomain() =
  when (this) {
    ScanIntervalDto.DISABLED -> Library.ScanInterval.DISABLED
    ScanIntervalDto.HOURLY -> Library.ScanInterval.HOURLY
    ScanIntervalDto.EVERY_6H -> Library.ScanInterval.EVERY_6H
    ScanIntervalDto.EVERY_12H -> Library.ScanInterval.EVERY_12H
    ScanIntervalDto.DAILY -> Library.ScanInterval.DAILY
    ScanIntervalDto.WEEKLY -> Library.ScanInterval.WEEKLY
  }
