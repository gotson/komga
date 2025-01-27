package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailSize

enum class ThumbnailSizeDto {
  DEFAULT,
  MEDIUM,
  LARGE,
  XLARGE,
}

fun ThumbnailSize.toDto() =
  when (this) {
    ThumbnailSize.DEFAULT -> ThumbnailSizeDto.DEFAULT
    ThumbnailSize.MEDIUM -> ThumbnailSizeDto.MEDIUM
    ThumbnailSize.LARGE -> ThumbnailSizeDto.LARGE
    ThumbnailSize.XLARGE -> ThumbnailSizeDto.XLARGE
  }

fun ThumbnailSizeDto.toDomain() =
  when (this) {
    ThumbnailSizeDto.DEFAULT -> ThumbnailSize.DEFAULT
    ThumbnailSizeDto.MEDIUM -> ThumbnailSize.MEDIUM
    ThumbnailSizeDto.LARGE -> ThumbnailSize.LARGE
    ThumbnailSizeDto.XLARGE -> ThumbnailSize.XLARGE
  }
