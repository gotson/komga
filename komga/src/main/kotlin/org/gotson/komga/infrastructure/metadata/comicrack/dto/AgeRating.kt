package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.annotation.JsonCreator

enum class AgeRating(
  val value: String,
  val ageRating: Int? = null,
) {
  UNKNOWN("Unknown"),
  ADULTS_ONLY_18("Adults Only 18+", 18),
  EARLY_CHILDHOOD("Early Childhood", 3),
  EVERYONE("Everyone", 0),
  EVERYONE_10("Everyone 10+", 10),
  G("G", 0),
  KIDS_TO_ADULTS("Kids to Adults", 6),
  M("M", 17),
  MA_15("MA 15+", 15),
  MATURE_17("Mature 17+", 17),
  PG("PG", 8),
  R_18("R18+", 18),
  RATING_PENDING("Rating Pending"),
  TEEN("Teen", 13),
  X_18("X18+", 18),
  ;

  companion object {
    private val map = entries.associateBy { it.value.toLowerNoSpace() }

    @JvmStatic
    @JsonCreator
    fun fromValue(value: String) = map[value.toLowerNoSpace()]

    private fun String.toLowerNoSpace() = lowercase().replace(" ", "")
  }
}
