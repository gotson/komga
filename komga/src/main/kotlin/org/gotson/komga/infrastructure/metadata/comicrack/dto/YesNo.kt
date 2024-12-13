package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.annotation.JsonCreator

enum class YesNo(
  val value: String,
) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes"),
  ;

  companion object {
    private val map = values().associateBy(YesNo::value)

    @JvmStatic
    @JsonCreator
    fun fromValue(value: String) = map[value]
  }
}
