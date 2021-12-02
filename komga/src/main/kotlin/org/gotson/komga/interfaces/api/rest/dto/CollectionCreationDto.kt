package org.gotson.komga.interfaces.api.rest.dto

import org.hibernate.validator.constraints.UniqueElements
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class CollectionCreationDto(
  @get:NotBlank val name: String,
  val ordered: Boolean,
  @get:NotEmpty @get:UniqueElements val seriesIds: List<String>
)
