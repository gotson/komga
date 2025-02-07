package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClientSettingDto(
  val value: String,
  val allowUnauthorized: Boolean?,
)

data class ClientSettingGlobalUpdateDto(
  @get:NotBlank
  val value: String,
  val allowUnauthorized: Boolean,
)

data class ClientSettingUserUpdateDto(
  @get:NotBlank
  val value: String,
)
