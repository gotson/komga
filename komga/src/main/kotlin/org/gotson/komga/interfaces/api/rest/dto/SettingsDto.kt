package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SettingsDto(
  val deleteEmptyCollections: Boolean? = null,
  val deleteEmptyReadLists: Boolean? = null,
  val rememberMeDurationDays: Long? = null,
  val thumbnailSize: ThumbnailSizeDto? = null,
  val taskPoolSize: Int? = null,
  val serverPort: SettingMultiSource<Int>? = null,
  val serverContextPath: SettingMultiSource<String>? = null,
  val koboProxy: Boolean? = null,
  val koboPort: Int? = null,
  val kepubifyPath: SettingMultiSource<String>? = null,
  val maxUploadFileSizeBytes: Long? = null,
)

fun SettingsDto.public() =
  SettingsDto(
    maxUploadFileSizeBytes = this.maxUploadFileSizeBytes,
  )

data class SettingMultiSource<T>(
  val configurationSource: T?,
  val databaseSource: T?,
  val effectiveValue: T?,
)
