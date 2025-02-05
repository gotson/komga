package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClientSettingDto(
  val key: String,
  val value: String,
  val allowUnauthorized: Boolean?,
  val userId: String?,
)

data class ClientSettingGlobalUpdateDto(
  val key: String,
  val value: String,
  val allowUnauthorized: Boolean,
)

data class ClientSettingUserUpdateDto(
  val key: String,
  val value: String,
)
