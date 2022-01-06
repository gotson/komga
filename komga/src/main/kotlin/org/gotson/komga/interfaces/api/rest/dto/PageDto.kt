package org.gotson.komga.interfaces.api.rest.dto

import com.jakewharton.byteunits.BinaryByteUnit

data class PageDto(
  val number: Int,
  val fileName: String,
  val mediaType: String,
  val width: Int?,
  val height: Int?,
  val sizeBytes: Long?,
  val size: String = sizeBytes?.let { BinaryByteUnit.format(it) } ?: "",
)
