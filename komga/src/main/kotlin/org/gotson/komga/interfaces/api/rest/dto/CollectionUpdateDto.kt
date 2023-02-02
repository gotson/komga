package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import org.gotson.komga.infrastructure.validation.NullOrNotEmpty
import org.hibernate.validator.constraints.UniqueElements

data class CollectionUpdateDto(
  @get:NullOrNotBlank val name: String?,
  val ordered: Boolean?,
  @get:NullOrNotEmpty @get:UniqueElements
  val seriesIds: List<String>?,
)
