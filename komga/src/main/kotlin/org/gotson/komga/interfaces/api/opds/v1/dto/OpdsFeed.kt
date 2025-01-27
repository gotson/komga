package org.gotson.komga.interfaces.api.opds.v1.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.ZonedDateTime

@JacksonXmlRootElement(localName = "feed", namespace = ATOM)
abstract class OpdsFeed(
  @JacksonXmlProperty(namespace = ATOM)
  val id: String,
  @JacksonXmlProperty(namespace = ATOM)
  val title: String,
  @JacksonXmlProperty(namespace = ATOM)
  val updated: ZonedDateTime,
  @JacksonXmlProperty(namespace = ATOM)
  val author: OpdsAuthor,
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "link", namespace = ATOM)
  val links: List<OpdsLink>,
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "entry", namespace = ATOM)
  val entries: List<OpdsEntry>,
)

@JsonSerialize(`as` = OpdsFeed::class)
class OpdsFeedNavigation(
  id: String,
  title: String,
  updated: ZonedDateTime,
  author: OpdsAuthor,
  links: List<OpdsLink>,
  entries: List<OpdsEntryNavigation>,
) : OpdsFeed(id, title, updated, author, links, entries)

@JsonSerialize(`as` = OpdsFeed::class)
class OpdsFeedAcquisition(
  id: String,
  title: String,
  updated: ZonedDateTime,
  author: OpdsAuthor,
  links: List<OpdsLink>,
  entries: List<OpdsEntryAcquisition>,
) : OpdsFeed(id, title, updated, author, links, entries)
