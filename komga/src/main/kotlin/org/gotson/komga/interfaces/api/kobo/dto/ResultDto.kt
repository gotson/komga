package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class ResultDto {
  @JsonProperty("Success")
  SUCCESS,

  // Not sure what Kobo accepts exactly, so I made up my own
  @JsonProperty("Failure")
  FAILURE,

  @JsonProperty("Ignored")
  IGNORED,
  ;

  fun wrapped() = WrappedResultDto(this)
}
