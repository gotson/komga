package org.gotson.komga.interfaces.api.opds.v1.dto

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import jakarta.validation.constraints.Size

@JacksonXmlRootElement(localName = "OpenSearchDescription", namespace = OPENSEARCH)
class OpenSearchDescription(
  @JacksonXmlProperty(localName = "ShortName", namespace = OPENSEARCH)
  @Size(min = 1, max = 16)
  val shortName: String,
  @JacksonXmlProperty(localName = "Description", namespace = OPENSEARCH)
  @Size(min = 1, max = 1024)
  val description: String,
  @JacksonXmlProperty(localName = "InputEncoding", namespace = OPENSEARCH)
  val inputEncoding: String = "UTF-8",
  @JacksonXmlProperty(localName = "OutputEncoding", namespace = OPENSEARCH)
  val outputEncoding: String = "UTF-8",
  @JacksonXmlProperty(localName = "Url", namespace = OPENSEARCH)
  val url: OpenSearchUrl,
) {
  class OpenSearchUrl(
    @JacksonXmlProperty(isAttribute = true)
    val template: String,
  ) {
    @JacksonXmlProperty(isAttribute = true)
    val type = "application/atom+xml;profile=opds-catalog;kind=acquisition"
  }
}
