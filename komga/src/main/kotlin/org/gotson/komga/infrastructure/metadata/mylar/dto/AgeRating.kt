package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class AgeRating(
  @get:JsonValue val value: String,
  val ageRating: Int? = null,
) {
  ALL("All", 0),
  NINE("9+", 9),
  TWELVE("12+", 12),
  FIFTEEN("15+", 15),
  SEVENTEEN("17+", 17),
  ADULT("Adult", 18),
}
