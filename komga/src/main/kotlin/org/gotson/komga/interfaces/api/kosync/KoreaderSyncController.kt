package org.gotson.komga.interfaces.api.kosync

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.koreader.KoreaderProgressConverter
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.kosync.dto.DocumentProgressDto
import org.gotson.komga.interfaces.api.kosync.dto.UserAuthenticationDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/koreader", produces = [MediaType.APPLICATION_JSON_VALUE, "application/vnd.koreader.v1+json"])
class KoreaderSyncController(
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val bookLifecycle: BookLifecycle,
  private val koreaderProgressConverter: KoreaderProgressConverter,
) {
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

    val r2Progression =
      koreaderProgressConverter.convertKoreaderProgressToR2(koreaderProgress, BookWithMedia(book, media))
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    bookLifecycle.markProgression(book, principal.user, r2Progression)
  }
}
