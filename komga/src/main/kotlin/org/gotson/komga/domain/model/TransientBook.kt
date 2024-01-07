package org.gotson.komga.domain.model

data class TransientBook(
  val book: Book,
  val media: Media,
  val metadata: Metadata = Metadata(),
) {
  data class Metadata(
    val number: Float? = null,
    val seriesId: String? = null,
  )
}

fun TransientBook.toBookWithMedia() = BookWithMedia(book, media)
