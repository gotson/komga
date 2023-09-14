package org.gotson.komga.interfaces.api.dto

import org.springframework.http.MediaType

const val MEDIATYPE_OPDS_JSON_VALUE = "application/opds+json"
const val MEDIATYPE_DIVINA_JSON_VALUE = "application/divina+json"
const val MEDIATYPE_WEBPUB_JSON_VALUE = "application/webpub+json"

const val PROFILE_DIVINA = "https://readium.org/webpub-manifest/profiles/divina"
const val PROFILE_PDF = "https://readium.org/webpub-manifest/profiles/pdf"

val MEDIATYPE_OPDS_PUBLICATION_JSON = MediaType("application", "opds-publication+json")
val MEDIATYPE_DIVINA_JSON = MediaType("application", "divina+json")
val MEDIATYPE_WEBPUB_JSON = MediaType("application", "webpub+json")
