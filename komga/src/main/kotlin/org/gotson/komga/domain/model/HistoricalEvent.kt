package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.nio.file.Path
import java.time.LocalDateTime

sealed class HistoricalEvent(
  val type: String,
  val bookId: String? = null,
  val seriesId: String? = null,
  val properties: Map<String, String> = emptyMap(),
  val timestamp: LocalDateTime = LocalDateTime.now(),
  val id: String = TsidCreator.getTsid256().toString(),
) {
  class BookFileDeleted(
    book: Book,
    reason: String,
  ) : HistoricalEvent(
      type = "BookFileDeleted",
      bookId = book.id,
      seriesId = book.seriesId,
      properties =
        mapOf(
          "reason" to reason,
          "name" to book.path.toString(),
        ),
    )

  class SeriesFolderDeleted(
    seriesId: String,
    seriesPath: Path,
    reason: String,
  ) : HistoricalEvent(
      type = "SeriesFolderDeleted",
      seriesId = seriesId,
      properties =
        mapOf(
          "reason" to reason,
          "name" to seriesPath.toString(),
        ),
    ) {
    constructor(series: Series, reason: String) : this(series.id, series.path, reason)
  }

  class BookConverted(
    book: Book,
    previous: Book,
  ) : HistoricalEvent(
      type = "BookConverted",
      bookId = book.id,
      seriesId = book.seriesId,
      properties =
        mapOf(
          "name" to book.path.toString(),
          "former file" to previous.path.toString(),
        ),
    )

  class BookImported(
    book: Book,
    series: Series,
    source: Path,
    upgrade: Boolean,
  ) : HistoricalEvent(
      type = "BookImported",
      bookId = book.id,
      seriesId = series.id,
      properties =
        mapOf(
          "name" to book.path.toString(),
          "source" to source.toString(),
          "upgrade" to if (upgrade) "Yes" else "No",
        ),
    )

  class DuplicatePageDeleted(
    book: Book,
    page: BookPageNumbered,
  ) : HistoricalEvent(
      type = "DuplicatePageDeleted",
      bookId = book.id,
      seriesId = book.seriesId,
      properties =
        mapOf(
          "name" to book.path.toString(),
          "page number" to page.pageNumber.toString(),
          "page file name" to page.fileName,
          "page file hash" to page.fileHash,
          "page file size" to page.fileSize.toString(),
          "page media type" to page.mediaType,
        ),
    )
}
