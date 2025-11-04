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

data class PluginUpdateCheckDto(
  val pluginId: String,
  val pluginName: String,
  val currentVersion: String,
  val latestVersion: String?,
  val updateAvailable: Boolean,
  val releaseUrl: String?,
  val releaseNotes: String?,
  val error: String?,
)

data class ChapterCheckResultDto(
  val seriesId: String,
  val seriesTitle: String,
  val hasNewChapters: Boolean,
  val latestChapterNumber: String?,
  val latestChapterTitle: String?,
  val latestChapterUrl: String?,
  val newChapterCount: Int,
  val lastChecked: Long,
  val source: String,
  val message: String?,
)
