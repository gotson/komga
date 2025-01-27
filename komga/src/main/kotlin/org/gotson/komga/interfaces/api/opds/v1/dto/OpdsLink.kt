package org.gotson.komga.interfaces.api.opds.v1.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime

open class OpdsLink(
  @get:JacksonXmlProperty(isAttribute = true)
  val type: String,
  @get:JacksonXmlProperty(isAttribute = true)
  val rel: String,
  @get:JacksonXmlProperty(isAttribute = true)
  val href: String,
)

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFeedNavigation(
  rel: String,
  href: String,
) : OpdsLink(
    type = "application/atom+xml;profile=opds-catalog;kind=navigation",
    rel = rel,
    href = href,
  )

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFeedAcquisition(
  rel: String,
  href: String,
) : OpdsLink(
    type = "application/atom+xml;profile=opds-catalog;kind=acquisition",
    rel = rel,
    href = href,
  )

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkImage(
  mediaType: String,
  href: String,
) : OpdsLink(
    type = mediaType,
    rel = "http://opds-spec.org/image",
    href = href,
  )

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkImageThumbnail(
  mediaType: String,
  href: String,
) : OpdsLink(
    type = mediaType,
    rel = "http://opds-spec.org/image/thumbnail",
    href = href,
  )

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkFileAcquisition(
  mediaType: String?,
  href: String,
) : OpdsLink(
    type = mediaType ?: "application/octet-stream",
    rel = "http://opds-spec.org/acquisition",
    href = href,
  )

@JsonSerialize(`as` = OpdsLink::class)
class OpdsLinkSearch(
  href: String,
) : OpdsLink(
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
  @get:JacksonXmlProperty(isAttribute = true, namespace = OPDS_PSE)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val lastReadDate: LocalDateTime?,
) : OpdsLink(
    type = mediaType,
    rel = "http://vaemendis.net/opds-pse/stream",
    href = href,
  )
