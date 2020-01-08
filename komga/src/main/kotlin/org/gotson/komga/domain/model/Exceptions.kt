package org.gotson.komga.domain.model

class MediaNotReadyException : Exception()
class ImageConversionException(message: String) : Exception(message)
class DirectoryNotFoundException(message: String) : Exception(message)
class DuplicateNameException(message: String) : Exception(message)
class PathContainedInPath(message: String) : Exception(message)
