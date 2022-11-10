package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.UniqueElements

data class CollectionCreationDto(
  @get:NotBlank val name: String,
  val ordered: Boolean,
  @get:NotEmpty @get:UniqueElements
  val seriesIds: List<String>,
)
