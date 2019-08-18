package org.gotson.komga.interfaces.web

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Serie
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.gotson.komga.domain.service.BookManager
import org.gotson.komga.domain.service.MetadataNotReadyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File

@RestController
@RequestMapping("api/v1/series")
class SerieController(
    private val serieRepository: SerieRepository,
    private val bookRepository: BookRepository,
    private val bookManager: BookManager
) {

  @GetMapping
  fun getAllSeries(page: Pageable) =
      serieRepository.findAll(page).map { it.toDto() }

  @GetMapping("{id}")
  fun getOneSerie(
      @PathVariable id: Long
  ): SerieDto =
      serieRepository.findByIdOrNull(id)?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("{id}/books")
  fun getAllBooksBySerie(
      @PathVariable id: Long,
      @RequestParam(value = "readyonly", defaultValue = "true") readyFilter: Boolean,
      page: Pageable
  ): Page<BookDto> {
    if (!serieRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return if (readyFilter) {
      bookRepository.findAllByMetadataStatusAndSerieId(Status.READY, id, page)
    } else {
      bookRepository.findAllBySerieId(id, page)
    }.map { it.toDto() }
  }

  @GetMapping("{serieId}/books/{bookId}")
  fun getOneBook(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long
  ): BookDto {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return bookRepository.findByIdOrNull(bookId)?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(value = ["{serieId}/books/{bookId}/content"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  fun getBookContent(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long
  ): ByteArray {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return bookRepository.findByIdOrNull(bookId)?.let {
      File(it.url.toURI()).readBytes()
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{serieId}/books/{bookId}/pages")
  fun getBookPages(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long
  ): List<PageDto> {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull((bookId))?.let {
      if (it.metadata.status == Status.UNKNOWN) throw ResponseStatusException(HttpStatus.NO_CONTENT, "Book is not parsed yet")
      if (it.metadata.status in listOf(Status.ERROR, Status.UNSUPPORTED)) throw ResponseStatusException(HttpStatus.NO_CONTENT, "Book cannot be parsed")

      it.metadata.pages.mapIndexed { index, s -> PageDto(index + 1, s.fileName, s.mediaType) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{serieId}/books/{bookId}/pages/{pageNumber}")
  fun getBookPage(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long,
      @PathVariable pageNumber: Int
  ): ResponseEntity<ByteArray> {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)

    try {
      return bookRepository.findByIdOrNull((bookId))?.let { book ->
        val pageContent = bookManager.getPageContent(book, pageNumber)

        val mediaType = try {
          MediaType.parseMediaType(book.metadata.mediaType!!)
        } catch (ex: Exception) {
          MediaType.APPLICATION_OCTET_STREAM
        }

        ResponseEntity.ok()
            .contentType(mediaType)
            .body(pageContent)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } catch (ex: ArrayIndexOutOfBoundsException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number does not exist")
    } catch (ex: MetadataNotReadyException) {
      throw ResponseStatusException(HttpStatus.NO_CONTENT, "Book cannot be parsed")
    }
  }
}

data class SerieDto(
    val id: Long,
    val name: String,
    val url: String
)

fun Serie.toDto() = SerieDto(
    id = id,
    name = name,
    url = url.toString()
)


data class BookDto(
    val id: Long,
    val name: String,
    val url: String,
    val metadata: BookMetadataDto
)

data class BookMetadataDto(
    val status: String,
    val mediaType: String
)

fun Book.toDto() =
    BookDto(
        id = id,
        name = name,
        url = url.toString(),
        metadata = BookMetadataDto(
            status = metadata.status.toString(),
            mediaType = metadata.mediaType ?: ""
        )
    )

data class PageDto(
    val number: Int,
    val fileName: String,
    val mediaType: String
)