package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
enum class TagTypeDto {
  SYSTEM_TAG,
  USER_TAG,
}
