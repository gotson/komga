package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class ReadingList {
  @JsonProperty(value = "Name")
  var name: String? = null

  @JacksonXmlElementWrapper(useWrapping = true)
  @JacksonXmlProperty(localName = "Books")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  var books: List<Book> = emptyList()

  override fun toString(): String = "ReadingList(name=$name, books=$books)"
}
