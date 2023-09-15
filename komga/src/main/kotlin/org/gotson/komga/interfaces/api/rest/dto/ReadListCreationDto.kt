package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.UniqueElements

data class ReadListCreationDto(
  @get:NotBlank val name: String,
  val summary: String = "",
  val ordered: Boolean = true,
  @get:NotEmpty @get:UniqueElements
  val bookIds: List<String>,
)
