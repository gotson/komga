package org.gotson.komga.interfaces.api.rest.dto

import com.jakewharton.byteunits.BinaryByteUnit
import org.gotson.komga.domain.model.PageHashUnknown

data class PageHashUnknownDto(
  val hash: String,
  val mediaType: String,
  val sizeBytes: Long?,
  val size: String? = sizeBytes?.let { BinaryByteUnit.format(it) },
  val totalSize: String ? = sizeBytes?.let { BinaryByteUnit.format(it * matchCount) },
  val matchCount: Int,
)

fun PageHashUnknown.toDto() = PageHashUnknownDto(
  hash = hash,
  mediaType = mediaType,
  sizeBytes = size,
  matchCount = matchCount,
)
