package org.gotson.komga.interfaces.rest.dto

import org.hibernate.validator.constraints.UniqueElements
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ReadListCreationDto(
  @get:NotBlank val name: String,
  @get:NotEmpty @get:UniqueElements val bookIds: List<String>
)
