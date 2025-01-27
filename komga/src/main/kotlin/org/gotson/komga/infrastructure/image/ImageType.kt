package org.gotson.komga.infrastructure.image

enum class ImageType(
  val mediaType: String,
  val imageIOFormat: String,
) {
  PNG("image/png", "PNG"),
  JPEG("image/jpeg", "JPEG"),
}
