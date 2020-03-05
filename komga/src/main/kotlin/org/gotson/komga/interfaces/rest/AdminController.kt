package org.gotson.komga.interfaces.rest

import mu.KotlinLogging
import org.gotson.komga.application.service.AsyncOrchestrator
import org.gotson.komga.domain.persistence.BookRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.RejectedExecutionException

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
  private val asyncOrchestrator: AsyncOrchestrator,
  private val bookRepository: BookRepository
) {

  @PostMapping("rpc/thumbnails/regenerate/all")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateAllThumbnails() {
    try {
      logger.info { "Regenerate thumbnail for all books" }
      asyncOrchestrator.generateThumbnails(bookRepository.findAll())
    } catch (e: RejectedExecutionException) {
      throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Thumbnail regeneration task is already running")
    }
  }

  @PostMapping("rpc/thumbnails/regenerate/missing")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateMissingThumbnails() {
    try {
      logger.info { "Regenerate missing thumbnails" }
      asyncOrchestrator.generateThumbnails(bookRepository.findAllByMediaThumbnailIsNull())
    } catch (e: RejectedExecutionException) {
      throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Thumbnail regeneration task is already running")
    }
  }
}
