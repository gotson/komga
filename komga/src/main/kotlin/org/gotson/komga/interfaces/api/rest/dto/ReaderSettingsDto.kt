package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ReaderSettings

data class ReaderSettingsDto(
  val userId: String,
  val seriesId: String?,
  val readingMode: String,
  val scaleType: String,
  val webtoonMode: Boolean,
  val continuousMode: Boolean,
  val doublePage: Boolean,
  val preloadPages: Int,
  val backgroundColor: String,
  val pageGap: Int,
  val cropBorders: Boolean,
  val rotatePages: Boolean,
  val invertColors: Boolean,
  val customBrightness: Int?,
  val id: String,
)

data class ReaderSettingsUpdateDto(
  val readingMode: String?,
  val scaleType: String?,
  val webtoonMode: Boolean?,
  val continuousMode: Boolean?,
  val doublePage: Boolean?,
  val preloadPages: Int?,
  val backgroundColor: String?,
  val pageGap: Int?,
  val cropBorders: Boolean?,
  val rotatePages: Boolean?,
  val invertColors: Boolean?,
  val customBrightness: Int?,
)

data class PagePreloadDto(
  val bookId: String,
  val pageNumbers: List<Int>,
  val seriesId: String,
  val nextBookId: String?,
)

fun ReaderSettings.toDto() =
  ReaderSettingsDto(
    userId = userId,
    seriesId = seriesId,
    readingMode = readingMode.name,
    scaleType = scaleType.name,
    webtoonMode = webtoonMode,
    continuousMode = continuousMode,
    doublePage = doublePage,
    preloadPages = preloadPages,
    backgroundColor = backgroundColor,
    pageGap = pageGap,
    cropBorders = cropBorders,
    rotatePages = rotatePages,
    invertColors = invertColors,
    customBrightness = customBrightness,
    id = id,
  )

fun ReaderSettings.patch(update: ReaderSettingsUpdateDto): ReaderSettings {
  return copy(
    readingMode = update.readingMode?.let { ReaderSettings.ReadingMode.valueOf(it) } ?: readingMode,
    scaleType = update.scaleType?.let { ReaderSettings.ScaleType.valueOf(it) } ?: scaleType,
    webtoonMode = update.webtoonMode ?: webtoonMode,
    continuousMode = update.continuousMode ?: continuousMode,
    doublePage = update.doublePage ?: doublePage,
    preloadPages = update.preloadPages ?: preloadPages,
    backgroundColor = update.backgroundColor ?: backgroundColor,
    pageGap = update.pageGap ?: pageGap,
    cropBorders = update.cropBorders ?: cropBorders,
    rotatePages = update.rotatePages ?: rotatePages,
    invertColors = update.invertColors ?: invertColors,
    customBrightness = update.customBrightness ?: customBrightness,
    lastModifiedDate = java.time.LocalDateTime.now(),
  )
}
