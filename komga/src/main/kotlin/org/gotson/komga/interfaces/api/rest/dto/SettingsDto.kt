package org.gotson.komga.interfaces.api.rest.dto

data class SettingsDto(
  val deleteEmptyCollections: Boolean,
  val deleteEmptyReadLists: Boolean,
  val rememberMeDurationDays: Long,
  val thumbnailSize: ThumbnailSizeDto,
  val taskPoolSize: Int,
  val serverPort: SettingMultiSource<Int?>,
  val serverContextPath: SettingMultiSource<String?>,
  val koboProxy: Boolean,
  val koboPort: Int?,
  val kepubifyPath: SettingMultiSource<String?>,
)

data class SettingMultiSource<T>(
  val configurationSource: T,
  val databaseSource: T,
  val effectiveValue: T,
)
