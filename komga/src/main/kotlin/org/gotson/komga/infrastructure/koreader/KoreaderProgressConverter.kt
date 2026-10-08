package org.gotson.komga.infrastructure.koreader

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.R2Device
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.R2Progression
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.interfaces.api.kosync.dto.DocumentProgressDto
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@Component
class KoreaderProgressConverter(
  private val mediaRepository: MediaRepository,
) {
  // convert the KOReader update request to an R2Progression
  fun convertKoreaderProgressToR2(
    koreaderProgress: DocumentProgressDto,
    bookWithMedia: BookWithMedia,
  ): R2Progression? {
    val locator =
      when (bookWithMedia.media.profile) {
        MediaProfile.DIVINA, MediaProfile.PDF ->
          R2Locator(
            href = "",
            type = "",
            locations =
              R2Locator.Location(
                position = koreaderProgress.progress.toInt(),
                totalProgression = koreaderProgress.percentage,
              ),
          )

        MediaProfile.EPUB -> {
          val resourceIndex =
            KoreaderUtils.parseResourceIndexFromProgressString(koreaderProgress.progress)
              ?: return null.also { logger.error { "Could not get Epub resource index from progress: ${koreaderProgress.progress}" } }

          val extension =
            (mediaRepository.findExtensionByIdOrNull(bookWithMedia.book.id) as? MediaExtensionEpub)
              ?: return null.also { logger.error { "Epub extension not found for book ${bookWithMedia.book.id}. Book should be re-analyzed." } }

          // get the href from the index provided by KOReader
          val href =
            extension.positions
              .groupBy { it.href }
              .keys
              .elementAt(resourceIndex)

          R2Locator(
            href = href,
            // assume default, will be overwritten by the correct type when saved
            type = "application/xhtml+xml",
            locations =
              R2Locator.Location(
                progression = 0F,
                totalProgression = koreaderProgress.percentage,
              ),
          )
        }

        null -> return null.also { logger.error { "Book has no media profile" } }
      }

    return R2Progression(
      device =
        R2Device(
          id = koreaderProgress.deviceId,
          name = koreaderProgress.device,
        ),
      modified = ZonedDateTime.now(),
      locator = locator,
    )
  }
}
