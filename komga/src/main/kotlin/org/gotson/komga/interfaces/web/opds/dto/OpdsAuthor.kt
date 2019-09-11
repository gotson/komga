package org.gotson.komga.interfaces.web.opds.dto

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.net.URI

data class OpdsAuthor(
    @JacksonXmlProperty(namespace = ATOM)
    val name: String,

    @JacksonXmlProperty(namespace = ATOM)
    val uri: URI
)