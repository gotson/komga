package org.gotson.komga.interfaces.rest

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

@RestController
@RequestMapping("api/v1/filesystem", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ROLE_ADMIN')")
class FileSystemController {

  private val fs = FileSystems.getDefault()

  @GetMapping
  fun getDirectoryListing(
      @RequestParam(name = "path", required = false) path: String?
  ): DirectoryListingDto =
      if (path.isNullOrEmpty()) {
        DirectoryListingDto(
            directories = fs.rootDirectories.map { it.toDto() }
        )
      } else {
        val p = fs.getPath(path)
        if (!p.isAbsolute) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Path must be absolute")
        try {
          DirectoryListingDto(
              parent = (p.parent ?: "").toString(),
              directories = Files.list(p).use { dirStream ->
                dirStream.asSequence()
                    .filter { Files.isDirectory(it) }
                    .filter { !Files.isHidden(it) }
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() })
                    .map { it.toDto() }
                    .toList()
              }
          )
        } catch (e: Exception) {
          throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Path does not exist")
        }
      }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DirectoryListingDto(
    val parent: String? = null,
    val directories: List<PathDto>
)

data class PathDto(
    val type: String,
    val name: String,
    val path: String
)

fun Path.toDto(): PathDto =
    PathDto(
        type = if (Files.isDirectory(this)) "directory" else "file",
        name = (fileName ?: this).toString(),
        path = toString()
    )
