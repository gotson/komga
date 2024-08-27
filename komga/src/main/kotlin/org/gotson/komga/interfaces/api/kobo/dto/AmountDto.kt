package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AmountDto(
  val currencyCode: String? = null,
  val totalAmount: Int,
)
