package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.SeriesMetadata

data class SeriesMetadataUpdateDto(
  val status: SeriesMetadata.Status?
)
