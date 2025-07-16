package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.annotation.JsonCreator

enum class Manga(
  private val value: String,
) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes"),
  YES_AND_RIGHT_TO_LEFT("YesAndRightToLeft"),
  ;

  companion object {
    private val map = entries.associateBy(Manga::value)

    @JvmStatic
    @JsonCreator
    fun fromValue(value: String) = map[value]
  }
}
