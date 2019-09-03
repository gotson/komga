package org.gotson.komga.domain.model

class MetadataNotReadyException : Exception()
class UnsupportedMediaTypeException(msg: String, val mediaType: String) : Exception(msg)