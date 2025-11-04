package org.gotson.komga.domain.model

/**
 * Age rating system for content restriction.
 * Supports multiple rating systems (ESRB, PEGI, FSK, etc.)
 */
enum class AgeRating(val minimumAge: Int, val requiresAuth: Boolean) {
  // Universal - no restrictions
  UNKNOWN(0, false),
  ALL_AGES(0, false),
  EVERYONE(0, false),

  // Mild restrictions - no auth required
  EVERYONE_10(10, false),
  TEEN(13, false),
  PEGI_12(12, false),
  PEGI_16(16, false),

  // Adult content - requires authentication
  MATURE_17(17, true),
  MATURE(18, true),
  ADULTS_ONLY(18, true),
  PEGI_18(18, true),
  FSK_18(18, true),
  X_RATED(18, true),

  // Explicit adult content
  EXPLICIT(21, true);

  /**
   * Check if this rating requires authentication to access.
   */
  fun requiresAuthentication(): Boolean = requiresAuth

  /**
   * Check if this rating is adult content.
   */
  fun isAdultContent(): Boolean = minimumAge >= 18

  /**
   * Check if this rating is explicit content.
   */
  fun isExplicitContent(): Boolean = minimumAge >= 21

  companion object {
    /**
     * Parse age rating from string.
     */
    fun fromString(rating: String?): AgeRating {
      if (rating == null) return UNKNOWN

      val normalized = rating.uppercase().replace(" ", "_").replace("-", "_")

      return when {
        normalized in listOf("EVERYONE", "E", "ALL", "ALL_AGES") -> EVERYONE
        normalized in listOf("E10+", "EVERYONE_10", "E10") -> EVERYONE_10
        normalized in listOf("TEEN", "T", "13+") -> TEEN
        normalized in listOf("MATURE", "M", "17+") -> MATURE_17
        normalized in listOf("ADULTS_ONLY", "AO", "18+") -> ADULTS_ONLY
        normalized.contains("PEGI") && normalized.contains("12") -> PEGI_12
        normalized.contains("PEGI") && normalized.contains("16") -> PEGI_16
        normalized.contains("PEGI") && normalized.contains("18") -> PEGI_18
        normalized.contains("FSK") && normalized.contains("18") -> FSK_18
        normalized in listOf("X", "X_RATED", "XXX") -> X_RATED
        normalized in listOf("EXPLICIT", "HENTAI", "ADULT") -> EXPLICIT
        else -> {
          // Try to parse as numeric age
          rating.filter { it.isDigit() }.toIntOrNull()?.let { age ->
            when {
              age == 0 -> EVERYONE
              age <= 10 -> EVERYONE_10
              age <= 13 -> TEEN
              age <= 17 -> MATURE_17
              age <= 21 -> MATURE
              else -> EXPLICIT
            }
          } ?: UNKNOWN
        }
      }
    }

    /**
     * Get all ratings that require authentication.
     */
    fun getAuthRequiredRatings(): List<AgeRating> {
      return values().filter { it.requiresAuthentication() }
    }

    /**
     * Get all adult content ratings.
     */
    fun getAdultRatings(): List<AgeRating> {
      return values().filter { it.isAdultContent() }
    }
  }
}

/**
 * Content warning tags for additional restrictions.
 */
enum class ContentWarning {
  VIOLENCE,
  SEXUAL_CONTENT,
  NUDITY,
  LANGUAGE,
  DRUG_USE,
  GORE,
  HORROR,
  DISCRIMINATION,
  SELF_HARM,
  SENSITIVE_CONTENT;

  companion object {
    fun fromString(warning: String): ContentWarning? {
      return values().find { it.name.equals(warning, ignoreCase = true) }
    }
  }
}
