package org.gotson.komga.interfaces.api.kosync

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.R2Device
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.R2Progression
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.kosync.dto.DocumentProgressDto
import org.gotson.komga.interfaces.api.kosync.dto.UserAuthenticationDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/koreader", produces = ["application/vnd.koreader.v1+json"])
class KoreaderSyncController(
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val bookLifecycle: BookLifecycle,
) {
  private val resourceRegex1 = Regex("""DocFragment\[(\d+)]""", RegexOption.IGNORE_CASE)
  private val resourceRegex2 = Regex("""#_doc_fragment_(\d+)_""", RegexOption.IGNORE_CASE)

  @PostMapping("users/create")
  fun registerUser(): ResponseEntity<String> = throw ResponseStatusException(HttpStatus.FORBIDDEN, "User creation is disabled")

  @GetMapping("users/auth")
  fun authorize() = UserAuthenticationDto()

  @GetMapping("syncs/progress/{bookHash}")
  fun getProgress(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookHash: String,
  ): DocumentProgressDto {
    val books = bookRepository.findAllByHashKoreader(bookHash)
    if (books.isEmpty()) {
      logger.debug { "No book found with KOReader hash: $bookHash" }
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")
    }
    if (books.size > 1) {
      logger.debug { "No unique book found with KOReader hash: $bookHash. Found ${books.size} books with the same hash." }
      throw ResponseStatusException(HttpStatus.CONFLICT, "More than 1 book found with the same hash")
    }

    val book = books.first()
    val media = mediaRepository.findById(book.id)

    val readProgress =
      readProgressRepository.findByBookIdAndUserIdOrNull(book.id, principal.user.id)
        ?: throw ResponseStatusException(HttpStatus.OK, "No progress found for this book")

    val progressPercentage =
      readProgress
        .locator
        ?.locations
        ?.totalProgression
        ?: (readProgress.page.toFloat() / mediaRepository.findById(book.id).pageCount.toFloat())

    val progress =
      when (media.profile) {
        MediaProfile.DIVINA, MediaProfile.PDF -> readProgress.page.toString()
        MediaProfile.EPUB -> {
          val extension =
            mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
              ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Epub extension not found")
                .also { logger.error { "Epub extension not found for book ${book.id}. Book should be re-analyzed." } }

          // convert the href to its index for KOReader
          val resourceIndex =
            extension.positions
              .groupBy { it.href }
              .keys
              .indexOf(readProgress.locator?.href)

          // return a progress string that points to the beginning of the resource
          "/body/DocFragment[${resourceIndex + 1}].0"
        }

        null -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has no media profile")
      }

    return DocumentProgressDto(
      document = bookHash,
      percentage = progressPercentage,
      progress = progress,
      device = readProgress.deviceName,
      deviceId = readProgress.deviceId,
    )
  }

  @PutMapping("syncs/progress")
  fun updateProgress(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody koreaderProgress: DocumentProgressDto,
  ) {
    val books = bookRepository.findAllByHashKoreader(koreaderProgress.document)
    if (books.isEmpty()) {
      logger.debug { "No book found with KOReader hash: ${koreaderProgress.document}" }
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")
    }
    if (books.size > 1) {
      logger.debug { "No unique book found with KOReader hash: ${koreaderProgress.document}. Found ${books.size} books with the same hash." }
      throw ResponseStatusException(HttpStatus.CONFLICT, "More than 1 book found with the same hash")
    }

    val book = books.first()
    val media = mediaRepository.findById(book.id)

    // convert the KOReader update request to an R2Progression
    val locator =
      when (media.profile) {
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
            // we try to parse the progress using the 2 possible formats
            resourceRegex1
              .find(koreaderProgress.progress)
              ?.groups
              // capturing group is at index 1, 0 is the full match
              ?.get(1)
              ?.value
              ?.toIntOrNull()
              // KOReader indexing starts at 1, not 0
              ?.minus(1)
              ?: resourceRegex2
                .find(koreaderProgress.progress)
                ?.groups
                // capturing group is at index 1, 0 is the full match
                ?.get(1)
                ?.value
                ?.toIntOrNull()
              ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get Epub resource index from progress: ${koreaderProgress.progress}")
                .also { logger.error { "Could not get Epub resource index from progress: ${koreaderProgress.progress}" } }

          val extension =
            mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
              ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Epub extension not found")
                .also { logger.error { "Epub extension not found for book ${book.id}. Book should be re-analyzed." } }

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

        null -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has no media profile")
      }

    val r2Progression =
      R2Progression(
        device =
          R2Device(
            id = koreaderProgress.deviceId,
            name = koreaderProgress.device,
          ),
        modified = ZonedDateTime.now(),
        locator = locator,
      )

    bookLifecycle.markProgression(book, principal.user, r2Progression)
  }
}
