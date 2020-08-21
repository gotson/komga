package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import org.gotson.komga.infrastructure.validation.NullOrNotEmpty
import org.hibernate.validator.constraints.UniqueElements

data class ReadListUpdateDto(
  @get:NullOrNotBlank val name: String?,
  @get:NullOrNotEmpty @get:UniqueElements val bookIds: List<String>?
)
