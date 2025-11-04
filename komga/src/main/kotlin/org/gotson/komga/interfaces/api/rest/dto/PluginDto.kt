package org.gotson.komga.interfaces.api.rest.dto

import java.time.LocalDateTime

data class PluginDto(
  val id: String,
  val name: String,
  val version: String,
  val description: String,
  val author: String,
  val enabled: Boolean,
  val type: String,
  val capabilities: Set<String>,
  val jarPath: String?,
  val loadedAt: LocalDateTime?,
  val error: String?,
)

data class PluginUploadDto(
  val enabled: Boolean = false,
)

data class PluginStatusDto(
  val loaded: Int,
  val total: Int,
  val pluginDirectory: String,
)

data class PluginRepoDto(
  val id: String,
  val name: String,
  val owner: String,
  val repo: String,
  val description: String,
)
