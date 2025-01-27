package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.NotBlank

data class ApiKeyRequestDto(
  @get:NotBlank
  val comment: String,
)
