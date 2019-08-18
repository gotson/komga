package org.gotson.komga.domain.model

import javax.persistence.Embeddable

@Embeddable
class BookPage(
    val fileName: String,
    val mediaType: String
)