package org.gotson.komga.interfaces.web.opds.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class OpdsEntry(
    @get:JacksonXmlProperty(namespace = ATOM)
    val title: String,

    @get:JacksonXmlProperty(namespace = ATOM)
    val updated: ZonedDateTime,

    @get:JacksonXmlProperty(namespace = ATOM)
    val id: String,

    @get:JacksonXmlProperty(namespace = ATOM)
    val content: String
)

class OpdsEntryNavigation(
    title: String,
    updated: ZonedDateTime,
    id: String,
    content: String,

    @JacksonXmlProperty(namespace = ATOM)
    val link: OpdsLink
) : OpdsEntry(title, updated, id, content)

class OpdsEntryAcquisition(
    title: String,
    updated: ZonedDateTime,
    id: String,
    content: String,

    @JacksonXmlProperty(namespace = ATOM)
    val author: OpdsAuthor? = null,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "link", namespace = ATOM)
    val links: List<OpdsLink>
) : OpdsEntry(title, updated, id, content)