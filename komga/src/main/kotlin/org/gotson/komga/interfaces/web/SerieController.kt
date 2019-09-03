package org.gotson.komga.interfaces.web

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.klinq.jpaspec.likeLower
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.MetadataNotReadyException
import org.gotson.komga.domain.model.Serie
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.UnsupportedMediaTypeException
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.gotson.komga.domain.service.BookManager
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
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
import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/series")
class SerieController(
    private val serieRepository: SerieRepository,
    private val bookRepository: BookRepository,
    private val bookManager: BookManager
) {

  @GetMapping
  fun getAllSeries(
      @RequestParam("search")
      searchTerm: String?,

      page: Pageable
  ): Page<SerieDto> {
    val pageRequest = PageRequest.of(
        page.pageNumber,
        page.pageSize,
        if (page.sort.isSorted) page.sort
        else Sort.by(Sort.Order.asc("name").ignoreCase())
    )
    return if (!searchTerm.isNullOrEmpty()) {
      val spec = Serie::name.likeLower("%$searchTerm%")
      serieRepository.findAll(spec, pageRequest)
    } else {
      serieRepository.findAll(pageRequest)
    }.map { it.toDto() }
  }

  @GetMapping("/latest")
  fun getLatestSeries(
      page: Pageable
  ): Page<SerieDto> {
    val pageRequest = PageRequest.of(
        page.pageNumber,
        page.pageSize,
        Sort(Sort.Direction.DESC, "lastModifiedDate")
    )
    return serieRepository.findAll(pageRequest).map { it.toDto() }
  }

  @GetMapping("{id}")
  fun getOneSerie(
      @PathVariable id: Long
  ): SerieDto =
      serieRepository.findByIdOrNull(id)?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping(value = ["{serieId}/thumbnail"], produces = [MediaType.IMAGE_PNG_VALUE])
  fun getSerieThumbnail(
      @PathVariable serieId: Long
  ): ByteArray {
    return serieRepository.findByIdOrNull(serieId)?.let {
      it.books.firstOrNull()?.metadata?.thumbnail ?: throw ResponseStatusException(HttpStatus.NO_CONTENT)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{id}/books")
  fun getAllBooksBySerie(
      @PathVariable id: Long,
      @RequestParam(value = "readyonly", defaultValue = "true") readyFilter: Boolean,
      page: Pageable
  ): Page<BookDto> {
    if (!serieRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return if (readyFilter) {
      bookRepository.findAllByMetadataStatusAndSerieId(Status.READY.name, id, page)
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

  @GetMapping(value = ["{serieId}/books/{bookId}/thumbnail"], produces = [MediaType.IMAGE_PNG_VALUE])
  fun getBookThumbnail(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long
  ): ByteArray {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull(bookId)?.let {
      it.metadata.thumbnail ?: throw ResponseStatusException(HttpStatus.NO_CONTENT)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{serieId}/books/{bookId}/file")
  fun getBookFile(
      @PathVariable serieId: Long,
      @PathVariable bookId: Long
  ): ResponseEntity<ByteArray> {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return bookRepository.findByIdOrNull(bookId)?.let { book ->
      try {
        ResponseEntity.ok()
            .headers(HttpHeaders().apply {
              contentDisposition = ContentDisposition.builder("attachment")
                  .filename(FilenameUtils.getName(book.url.toString()))
                  .build()
            })
            .contentType(getMediaTypeOrDefault(book.metadata.mediaType))
            .body(File(book.url.toURI()).readBytes())
      } catch (ex: FileNotFoundException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
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
      @PathVariable pageNumber: Int,
      @RequestParam(value = "convert") convertTo: String?
  ): ResponseEntity<ByteArray> {
    if (!serieRepository.existsById(serieId)) throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull((bookId))?.let { book ->
      try {
        val convertFormat = when (convertTo?.toLowerCase()) {
          "jpeg" -> ImageType.JPEG
          "png" -> ImageType.PNG
          "", null -> null
          else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid conversion format: $convertTo")
        }

        val pageContent = try {
          bookManager.getBookPage(book, pageNumber, convertFormat)
        } catch (e: UnsupportedMediaTypeException) {
          throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
          throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        ResponseEntity.ok()
            .contentType(getMediaTypeOrDefault(pageContent.mediaType))
            .body(pageContent.content)
      } catch (ex: ArrayIndexOutOfBoundsException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number does not exist")
      } catch (ex: MetadataNotReadyException) {
        throw ResponseStatusException(HttpStatus.NO_CONTENT, "Book cannot be parsed")
      } catch (ex: NoSuchFileException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  private fun getMediaTypeOrDefault(mediaTypeString: String?): MediaType {
    mediaTypeString?.let {
      try {
        return MediaType.parseMediaType(mediaTypeString)
      } catch (ex: Exception) {
      }
    }
    return MediaType.APPLICATION_OCTET_STREAM
  }
}

data class SerieDto(
    val id: Long,
    val name: String,
    val url: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?
)

fun Serie.toDto() = SerieDto(
    id = id,
    name = name,
    url = url.toString(),
    lastModified = lastModifiedDate?.toUTC()
)


data class BookDto(
    val id: Long,
    val name: String,
    val url: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?,
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
        url = url.toURI().path,
        lastModified = lastModifiedDate?.toUTC(),
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

fun LocalDateTime.toUTC(): LocalDateTime =
    atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()