package org.gotson.komga.interfaces.api.opds.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

open class OpdsLink(
  @get:JacksonXmlProperty(isAttribute = true)
  val type: String,

  @get:JacksonXmlProperty(isAttribute = true)
  val rel: String,

  @get:JacksonXmlProperty(isAttribute = true)
  val href: String,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFeedNavigation(rel: String, href: String) : OpdsLink(
  type = "application/atom+xml;profile=opds-catalog;kind=navigation",
  rel = rel,
  href = href,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFeedAcquisition(rel: String, href: String) : OpdsLink(
  type = "application/atom+xml;profile=opds-catalog;kind=acquisition",
  rel = rel,
  href = href,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkImage(mediaType: String, href: String) : OpdsLink(
  type = mediaType,
  rel = "http://opds-spec.org/image",
  href = href,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkImageThumbnail(mediaType: String, href: String) : OpdsLink(
  type = mediaType,
  rel = "http://opds-spec.org/image/thumbnail",
  href = href,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFileAcquisition(mediaType: String?, href: String) : OpdsLink(
  type = mediaType ?: "application/octet-stream",
  rel = "http://opds-spec.org/acquisition",
  href = href,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkSearch(href: String) : OpdsLink(
  type = "application/opensearchdescription+xml",
  rel = "search",
  href = href,
)

class OpdsLinkPageStreaming(
  mediaType: String,
  href: String,

  @get:JacksonXmlProperty(isAttribute = true, namespace = OPDS_PSE)
  val count: Int,

  @get:JacksonXmlProperty(isAttribute = true, namespace = OPDS_PSE)
  val lastRead: Int?,
) : OpdsLink(
  type = mediaType,
  rel = "http://vaemendis.net/opds-pse/stream",
  href = href,
)

class OpdsLinkRel {
  companion object {
    const val SELF = "self"
    const val START = "start"
    const val PREVIOUS = "previous"
    const val NEXT = "next"
    const val SUBSECTION = "subsection"
    const val SORT_NEW = "http://opds-spec.org/sort/new"
    const val SORT_POPULAR = "http://opds-spec.org/sort/popular"
  }
}
