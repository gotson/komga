package org.gotson.komga.interfaces.api.rest

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.streams.asSequence

@RestController
@RequestMapping("api/v1/filesystem", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
class FileSystemController {
  private val fs = FileSystems.getDefault()

  @PostMapping
  fun getDirectoryListing(
    @RequestBody(required = false) request: DirectoryRequestDto = DirectoryRequestDto(),
  ): DirectoryListingDto =
    if (request.path.isEmpty()) {
      DirectoryListingDto(
        directories = fs.rootDirectories.map { it.toDto() },
        files = emptyList(),
      )
    } else {
      val path = fs.getPath(request.path)
      if (!path.isAbsolute) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Path must be absolute")
      val directory = if (path.isDirectory()) path else path.parent
      try {
        val (directories, files) =
          Files.list(directory).use { dirStream ->
            dirStream
              .asSequence()
              .filter { !Files.isHidden(it) && (if (!request.showFiles) Files.isDirectory(it) else true) }
              .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() })
              .map { it.toDto() }
              .toList()
              .partition { it.type == "directory" }
          }
        DirectoryListingDto(
          parent = (path.parent ?: "").toString(),
          directories = directories,
          files = files,
        )
      } catch (e: Exception) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Path does not exist")
      }
    }
}

data class DirectoryRequestDto(
  val path: String = "",
  val showFiles: Boolean = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DirectoryListingDto(
  val parent: String? = null,
  val directories: List<PathDto>,
  val files: List<PathDto>,
)

data class PathDto(
  val type: String,
  val name: String,
  val path: String,
)

fun Path.toDto(): PathDto =
  PathDto(
    type = if (Files.isDirectory(this)) "directory" else "file",
    name = (fileName ?: this).toString(),
    path = toString(),
  )
