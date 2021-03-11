package org.gotson.komga.domain.model

open class CodedException(message: String, val code: String) : Exception(message)
class MediaNotReadyException : Exception()
class MediaUnsupportedException(message: String, code: String = "") : CodedException(message, code)
class ImageConversionException(message: String, code: String = "") : CodedException(message, code)
class DirectoryNotFoundException(message: String, code: String = "") : CodedException(message, code)
class DuplicateNameException(message: String, code: String = "") : CodedException(message, code)
class PathContainedInPath(message: String, code: String = "") : CodedException(message, code)
class UserEmailAlreadyExistsException(message: String, code: String = "") : CodedException(message, code)
