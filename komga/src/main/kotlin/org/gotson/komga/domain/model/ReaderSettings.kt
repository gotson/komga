package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.time.LocalDateTime

/**
 * Reader settings for enhanced reading experience.
 * Similar to Tachidesk/Suwayomi reader features.
 */
data class ReaderSettings(
  val userId: String,
  val seriesId: String? = null, // null for global settings
  val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
  val scaleType: ScaleType = ScaleType.FIT_WIDTH,
  val webtoonMode: Boolean = false,
  val continuousMode: Boolean = false,
  val doublePage: Boolean = false,
  val preloadPages: Int = 3,
  val backgroundColor: String = "#000000",
  val pageGap: Int = 0,
  val cropBorders: Boolean = false,
  val rotatePages: Boolean = false,
  val invertColors: Boolean = false,
  val customBrightness: Int? = null, // 0-100, null for device default
  val id: String = TsidCreator.getTsid256().toString(),
  val createdDate: LocalDateTime = LocalDateTime.now(),
  val lastModifiedDate: LocalDateTime = createdDate,
) {
  enum class ReadingMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON,
    CONTINUOUS_VERTICAL,
    CONTINUOUS_HORIZONTAL,
  }

  enum class ScaleType {
    FIT_SCREEN,
    FIT_WIDTH,
    FIT_HEIGHT,
    ORIGINAL_SIZE,
    SMART_FIT,
  }
}
