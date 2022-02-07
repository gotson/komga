package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.PageHashKnown
import javax.validation.constraints.NotBlank

data class PageHashCreationDto(
  @get:NotBlank val hash: String,
  @get:NotBlank val mediaType: String,
  val size: Long? = null,
  val action: PageHashKnown.Action,
)
