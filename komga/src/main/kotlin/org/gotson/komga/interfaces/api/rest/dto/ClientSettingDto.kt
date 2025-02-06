package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClientSettingDto(
  val value: String,
  val allowUnauthorized: Boolean?,
)

data class ClientSettingGlobalUpdateDto(
  val value: String,
  val allowUnauthorized: Boolean,
)

data class ClientSettingUserUpdateDto(
  val value: String,
)
