package org.gotson.komga.domain.model

open class CodedException : Exception {
  val code: String

  constructor(cause: Throwable, code: String) : super(cause) {
    this.code = code
  }

  constructor(message: String, code: String) : super(message) {
    this.code = code
  }
}

fun Exception.withCode(code: String) = CodedException(this, code)

class MediaNotReadyException : Exception()

class NoThumbnailFoundException : Exception()

class MediaUnsupportedException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class ImageConversionException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class DirectoryNotFoundException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class DuplicateNameException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class PathContainedInPath(
  message: String,
  code: String = "",
) : CodedException(message, code)

class UserEmailAlreadyExistsException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class BookConversionException(
  message: String,
) : Exception(message)

class ComicRackListException(
  message: String,
  code: String = "",
) : CodedException(message, code)

class EntryNotFoundException(
  message: String,
) : Exception(message)
