package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import org.gotson.komga.infrastructure.validation.NullOrNotEmpty
import org.gotson.komga.infrastructure.validation.Unique

data class CollectionUpdateDto(
  @get:NullOrNotBlank val name: String?,
  val ordered: Boolean?,
  @get:NullOrNotEmpty @get:Unique val seriesIds: List<String>?
)
