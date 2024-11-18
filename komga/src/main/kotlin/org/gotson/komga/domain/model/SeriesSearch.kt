package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SeriesSearch(
  val condition: SearchCondition.Series? = null,
  val fullTextSearch: String? = null,
  @JsonIgnore
  @Deprecated("Used for backward compatibility only, use SearchOperator.BeginsWith instead")
  val regexSearch: Pair<String, SearchField>? = null,
)
