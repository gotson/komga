package org.gotson.komga.interfaces.api.rest.dto

data class SettingsDto(
  val deleteEmptyCollections: Boolean,
  val deleteEmptyReadLists: Boolean,
  val rememberMeDurationDays: Long,
  val thumbnailSize: ThumbnailSizeDto,
  val taskPoolSize: Int,
)
