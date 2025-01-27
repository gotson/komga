package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BookSearch(
  val condition: SearchCondition.Book? = null,
  val fullTextSearch: String? = null,
)
