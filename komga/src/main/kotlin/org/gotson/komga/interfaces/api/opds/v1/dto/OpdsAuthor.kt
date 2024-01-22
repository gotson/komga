package org.gotson.komga.interfaces.api.opds.v1.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.net.URI

data class OpdsAuthor(
  @JacksonXmlProperty(namespace = ATOM)
  val name: String,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JacksonXmlProperty(namespace = ATOM)
  val uri: URI? = null,
)
