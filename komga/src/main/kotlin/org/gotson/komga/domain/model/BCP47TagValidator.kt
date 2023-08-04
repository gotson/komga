package org.gotson.komga.domain.model

import com.ibm.icu.util.ULocale

object BCP47TagValidator {
  private val languages by lazy { ULocale.getISOLanguages().toSet() }

  fun isValid(value: String?): Boolean {
    if (value == null) return false
    return ULocale.forLanguageTag(value).let {
      it.language.isNotBlank() && languages.contains(it.language)
    }
  }

  fun normalize(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return try {
      ULocale.forLanguageTag(value).toLanguageTag()
    } catch (e: Exception) {
      ""
    }
  }
}
