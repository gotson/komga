package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaContainerEntry
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

@Service
class EpubExtractor(contentDetector: ContentDetector) : ZipExtractor(contentDetector) {

  override fun mediaTypes(): List<String> = listOf("application/epub+zip")

  override fun getEntries(path: Path): List<MediaContainerEntry> {
    ZipFile(path.toFile()).use { zip ->
      try {
        val packagePath = zip.getEntry("META-INF/container.xml").let { entry ->
          val container = zip.getInputStream(entry).use { Jsoup.parse(it, null, "") }
          container.getElementsByTag("rootfile").first().attr("full-path")
        }

        val opf = zip.getInputStream(zip.getEntry(packagePath)).use { Jsoup.parse(it, null, "") }

        val manifest = opf.select("manifest > item")
          .associate { it.attr("id") to ManifestItem(it.attr("id"), it.attr("href"), it.attr("media-type")) }

        val pages = opf.select("spine > itemref").map { it.attr("idref") }
          .mapNotNull { manifest[it] }
          .map { it.href }

        val images = pages.flatMap { pagePath ->
          val doc = zip.getInputStream(zip.getEntry(pagePath)).use { Jsoup.parse(it, null, "") }
          doc.getElementsByTag("img")
            .map { it.attr("src") }
            .map { Paths.get(pagePath).parent?.resolve(it).toString() }
        }

        return images.map { image ->
          MediaContainerEntry(image, manifest.values.first { it.href == image }.mediaType)
        }
      } catch (e: Exception) {
        logger.error(e) { "File is not a proper Epub, treating it as a zip file" }
        return super.getEntries(path)
      }
    }
  }

  private data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String
  )
}
