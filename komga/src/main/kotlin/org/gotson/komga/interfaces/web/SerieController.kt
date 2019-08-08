package org.gotson.komga.interfaces.web

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Serie
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.net.URL

@RestController
@RequestMapping("api/v1/series")
class SerieController(
    private val serieRepository: SerieRepository,
    private val bookRepository: BookRepository
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
      page: Pageable
  ): Page<BookDto> {
    if (!serieRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return bookRepository.findAllBySerieId(id, page).map { it.toDto() }
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
}

data class SerieDto(
    val id: Long,
    val name: String,
    val url: URL
)

fun Serie.toDto() = SerieDto(id!!, name, url)


data class BookDto(
    val id: Long,
    val name: String,
    val url: URL
)

fun Book.toDto() = BookDto(id!!, name, url)