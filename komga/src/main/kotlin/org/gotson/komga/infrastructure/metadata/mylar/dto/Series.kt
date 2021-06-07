package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Series(
  val metadata: List<MylarMetadata>
)
