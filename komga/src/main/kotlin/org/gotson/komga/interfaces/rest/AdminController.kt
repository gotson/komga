package org.gotson.komga.interfaces.rest

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.persistence.BookRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
  private val bookRepository: BookRepository,
  private val taskReceiver: TaskReceiver
) {

  @PostMapping("rpc/thumbnails/regenerate/all")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateAllThumbnails() {
    logger.info { "Regenerate thumbnail for all books" }
    bookRepository.findAll().forEach { taskReceiver.generateBookThumbnail(it) }
  }

  @PostMapping("rpc/thumbnails/regenerate/missing")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateMissingThumbnails() {
    logger.info { "Regenerate missing thumbnails" }
    bookRepository.findAllByMediaThumbnailIsNull().forEach { taskReceiver.generateBookThumbnail(it) }
  }
}
