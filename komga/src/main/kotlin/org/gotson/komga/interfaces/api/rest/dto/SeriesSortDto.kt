package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.Library

enum class SeriesSortDto {
  NAME_NATURAL,
  FILE_MODIFIED_ASC,
  FILE_MODIFIED_DESC,
}

fun Library.SeriesSort.toDto() = when (this) {
  Library.SeriesSort.NAME_NATURAL -> SeriesSortDto.NAME_NATURAL
  Library.SeriesSort.FILE_MODIFIED_ASC -> SeriesSortDto.FILE_MODIFIED_ASC
  Library.SeriesSort.FILE_MODIFIED_DESC -> SeriesSortDto.FILE_MODIFIED_DESC
}

fun SeriesSortDto.toDomain() = when (this) {
  SeriesSortDto.NAME_NATURAL -> Library.SeriesSort.NAME_NATURAL
  SeriesSortDto.FILE_MODIFIED_ASC -> Library.SeriesSort.FILE_MODIFIED_ASC
  SeriesSortDto.FILE_MODIFIED_DESC -> Library.SeriesSort.FILE_MODIFIED_DESC
}
