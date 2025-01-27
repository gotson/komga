package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.Library

enum class SeriesCoverDto {
  FIRST,
  FIRST_UNREAD_OR_FIRST,
  FIRST_UNREAD_OR_LAST,
  LAST,
}

fun Library.SeriesCover.toDto() =
  when (this) {
    Library.SeriesCover.FIRST -> SeriesCoverDto.FIRST
    Library.SeriesCover.FIRST_UNREAD_OR_FIRST -> SeriesCoverDto.FIRST_UNREAD_OR_FIRST
    Library.SeriesCover.FIRST_UNREAD_OR_LAST -> SeriesCoverDto.FIRST_UNREAD_OR_LAST
    Library.SeriesCover.LAST -> SeriesCoverDto.LAST
  }

fun SeriesCoverDto.toDomain() =
  when (this) {
    SeriesCoverDto.FIRST -> Library.SeriesCover.FIRST
    SeriesCoverDto.FIRST_UNREAD_OR_FIRST -> Library.SeriesCover.FIRST_UNREAD_OR_FIRST
    SeriesCoverDto.FIRST_UNREAD_OR_LAST -> Library.SeriesCover.FIRST_UNREAD_OR_LAST
    SeriesCoverDto.LAST -> Library.SeriesCover.LAST
  }
