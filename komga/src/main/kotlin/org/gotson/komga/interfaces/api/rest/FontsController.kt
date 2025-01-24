package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.language.contains
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.toPath

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(value = ["api/v1/fonts"], produces = [MediaType.APPLICATION_JSON_VALUE])
class FontsController(
  komgaProperties: KomgaProperties,
) {
  private val supportedExtensions = listOf("woff", "woff2", "ttf", "otf")
  private final val fonts: Map<String, List<Resource>>

  init {
    val resolver = PathMatchingResourcePatternResolver()
    val fontsEmbedded =
      try {
        resolver
          .getResources("/embeddedFonts/**/*.*")
          .filterNot { it.filename == null }
          .filter { supportedExtensions.contains(it.uri.toPath().extension, true) }
          .groupBy {
            it.uri
              .toPath()
              .parent.name
          }
      } catch (e: Exception) {
        logger.error(e) { "Could not load embedded fonts" }
        emptyMap()
      }

    val fontsDir = Path(komgaProperties.fonts.dataDirectory)
    val fontsAdditional =
      try {
        if (fontsDir.isDirectory() && fontsDir.isReadable()) {
          fontsDir
            .listDirectoryEntries()
            .filter { it.isDirectory() }
            .associate { dir ->
              dir.name to
                dir
                  .listDirectoryEntries()
                  .filter { it.isRegularFile() }
                  .filter { it.isReadable() }
                  .filter { supportedExtensions.contains(it.extension, true) }
                  .map { FileSystemResource(it) }
            }
        } else {
          emptyMap()
        }
      } catch (e: Exception) {
        logger.error(e) { "Could not load additional fonts" }
        emptyMap()
      }

    fonts = fontsEmbedded + fontsAdditional

    logger.info { "Fonts embedded: $fontsEmbedded" }
    logger.info { "Fonts discovered: $fontsAdditional" }
  }

  @GetMapping("families")
  fun listFonts(): Set<String> = fonts.keys

  @GetMapping("resource/{fontFamily}/{fontFile}")
  fun getFontFile(
    @PathVariable fontFamily: String,
    @PathVariable fontFile: String,
  ): ResponseEntity<Resource> {
    fonts[fontFamily]?.let { resources ->
      val resource = resources.firstOrNull { it.uri.toPath().name == fontFile } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
      return ResponseEntity
        .ok()
        .headers {
          it.contentDisposition =
            ContentDisposition
              .attachment()
              .filename(fontFile)
              .build()
        }.contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("resource/{fontFamily}/css", produces = ["text/css"])
  fun getFontFamilyAsCss(
    @PathVariable fontFamily: String,
  ): ResponseEntity<Resource> {
    fonts[fontFamily]?.let { files ->
      val groups = files.groupBy { getFontCharacteristics(it.uri.toPath().name) }

      val css =
        groups
          .map { (styleWeight, resources) -> buildFontFaceBlock(fontFamily, styleWeight, resources) }
          .joinToString(separator = "\n")

      return ResponseEntity
        .ok()
        .headers {
          it.contentDisposition =
            ContentDisposition
              .attachment()
              .filename("$fontFamily.css")
              .build()
        }.body(ByteArrayResource(css.toByteArray()))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  private fun buildFontFaceBlock(
    fontFamily: String,
    styleAndWeight: FontCharacteristics,
    fonts: List<Resource>,
  ): String {
    val srcBlock =
      fonts.joinToString(separator = ",", postfix = ";") { resource ->
        val path = resource.uri.toPath()
        """url('${path.name}') format('${path.extension}')"""
      }
    // language=CSS
    return """
      @font-face {
          font-family: '$fontFamily';
          src: $srcBlock
          font-weight: ${styleAndWeight.weight};
          font-style: ${styleAndWeight.style};
      }

      """.trimIndent()
  }

  private fun getFontCharacteristics(filename: String): FontCharacteristics {
    val style = if (filename.contains("italic", true)) "italic" else "normal"
    val weight = if (filename.contains("bold", true)) "bold" else "normal"
    return FontCharacteristics(style, weight)
  }

  private data class FontCharacteristics(
    val style: String,
    val weight: String,
  )
}
