package org.gotson.komga.infrastructure.image

enum class ImageType {
  ORIGINAL, PNG, JPEG
}

fun ImageType.toMediaType(): String? =
    when (this) {
      ImageType.ORIGINAL -> null
      ImageType.PNG -> "image/png"
      ImageType.JPEG -> "image/jpeg"
    }

fun ImageType.toImageIOFormat(): String? =
    when (this) {
      ImageType.ORIGINAL -> null
      ImageType.PNG -> "png"
      ImageType.JPEG -> "jpeg"
    }