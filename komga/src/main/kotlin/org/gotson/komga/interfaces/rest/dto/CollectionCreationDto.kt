package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.Unique
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class CollectionCreationDto(
  @get:NotBlank val name: String,
  val ordered: Boolean,
  @get:NotEmpty @get:Unique val seriesIds: List<String>
)
