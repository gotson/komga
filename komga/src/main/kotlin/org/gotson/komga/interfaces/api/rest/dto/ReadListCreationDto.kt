package org.gotson.komga.interfaces.api.rest.dto

import org.hibernate.validator.constraints.UniqueElements
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ReadListCreationDto(
  @get:NotBlank val name: String,
  val summary: String = "",
  val ordered: Boolean = true,
  @get:NotEmpty @get:UniqueElements
  val bookIds: List<String>,
)
