package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
enum class StatusDto {
  @JsonProperty("ReadyToRead")
  READY_TO_READ,

  @JsonProperty("Finished")
  FINISHED,

  @JsonProperty("Reading")
  READING,
}
