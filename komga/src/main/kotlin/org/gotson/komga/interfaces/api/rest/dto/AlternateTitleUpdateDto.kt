package org.gotson.komga.interfaces.api.rest.dto

import javax.validation.constraints.NotBlank

class AlternateTitleUpdateDto {
  @get:NotBlank
  val label: String? = null

  @get:NotBlank
  val title: String? = null
}
