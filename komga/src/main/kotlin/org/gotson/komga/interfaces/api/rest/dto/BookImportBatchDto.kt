package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.CopyMode

data class BookImportBatchDto(
  val books: List<BookImportDto> = emptyList(),
  val copyMode: CopyMode,
)

data class BookImportDto(
  val sourceFile: String,
  val seriesId: String,
  val upgradeBookId: String? = null,
  val destinationName: String? = null,
)
