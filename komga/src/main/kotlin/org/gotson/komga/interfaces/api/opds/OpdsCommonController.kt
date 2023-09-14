package org.gotson.komga.interfaces.api.opds

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.checkContentRestriction
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class OpdsCommonController(
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
) {

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = [
      "/opds/v1.2/books/{bookId}/thumbnail",
      "/opds/v2/books/{bookId}/thumbnail",
    ],
    produces = [MediaType.IMAGE_JPEG_VALUE],
  )
  fun getBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ByteArray {
    principal.user.checkContentRestriction(bookId, bookRepository, seriesMetadataRepository)
    val thumbnail = bookLifecycle.getThumbnail(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return if (thumbnail.type == ThumbnailBook.Type.GENERATED) {
      bookLifecycle.getBookPage(bookRepository.findByIdOrNull(bookId)!!, 1, ImageType.JPEG).content
    } else {
      bookLifecycle.getThumbnailBytes(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
  }
}
