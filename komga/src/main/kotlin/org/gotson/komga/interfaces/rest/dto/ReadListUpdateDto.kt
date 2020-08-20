package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import org.gotson.komga.infrastructure.validation.NullOrNotEmpty
import org.gotson.komga.infrastructure.validation.Unique

data class ReadListUpdateDto(
  @get:NullOrNotBlank val name: String?,
  @get:NullOrNotEmpty @get:Unique val bookIds: List<String>?
)
