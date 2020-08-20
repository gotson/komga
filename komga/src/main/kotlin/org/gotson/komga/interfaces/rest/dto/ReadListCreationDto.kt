package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.Unique
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ReadListCreationDto(
  @get:NotBlank val name: String,
  @get:NotEmpty @get:Unique val bookIds: List<String>
)
