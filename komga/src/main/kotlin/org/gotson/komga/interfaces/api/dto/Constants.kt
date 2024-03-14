package org.gotson.komga.interfaces.api.dto

import org.springframework.http.MediaType

const val MEDIATYPE_OPDS_JSON_VALUE = "application/opds+json"
const val MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE = "application/opds-publication+json"
const val MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE = "application/opds-authentication+json"
const val MEDIATYPE_DIVINA_JSON_VALUE = "application/divina+json"
const val MEDIATYPE_WEBPUB_JSON_VALUE = "application/webpub+json"
const val MEDIATYPE_POSITION_LIST_JSON_VALUE = "application/vnd.readium.position-list+json"
const val MEDIATYPE_PROGRESSION_JSON_VALUE = "application/vnd.readium.progression+json"

const val PROFILE_DIVINA = "https://readium.org/webpub-manifest/profiles/divina"
const val PROFILE_EPUB = "https://readium.org/webpub-manifest/profiles/epub"
const val PROFILE_PDF = "https://readium.org/webpub-manifest/profiles/pdf"

val MEDIATYPE_OPDS_PUBLICATION_JSON = MediaType("application", "opds-publication+json")
val MEDIATYPE_DIVINA_JSON = MediaType("application", "divina+json")
val MEDIATYPE_WEBPUB_JSON = MediaType("application", "webpub+json")
val MEDIATYPE_POSITION_LIST_JSON = MediaType("application", "vnd.readium.position-list+json")
val MEDIATYPE_PROGRESSION_JSON = MediaType("application", "vnd.readium.progression+json")
