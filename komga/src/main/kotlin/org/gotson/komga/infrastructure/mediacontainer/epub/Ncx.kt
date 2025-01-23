package org.gotson.komga.infrastructure.mediacontainer.epub

import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.infrastructure.util.getEntryBytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.web.util.UriUtils
import java.nio.file.Path
import kotlin.io.path.Path

private val possibleNcxItemIds = listOf("toc", "ncx", "ncxtoc")

fun EpubPackage.getNcxResource(): ResourceContent? =
  (manifest.values.firstOrNull { it.mediaType == "application/x-dtbncx+xml" } ?: manifest.values.firstOrNull { possibleNcxItemIds.contains(it.id) })?.let { ncx ->
    val href = normalizeHref(opfDir, ncx.href)
    zip.getEntryBytes(href)?.decodeToString()?.let { ncxContent ->
      ResourceContent(Path(href), ncxContent)
    }
  }

fun processNcx(
  document: ResourceContent,
  navType: Epub2Nav,
): List<EpubTocEntry> =
  Jsoup
    .parse(document.content)
    .select("${navType.level1} > ${navType.level2}")
    .toList()
    .mapNotNull { ncxElementToTocEntry(navType, it, document.path.parent) }

private fun ncxElementToTocEntry(
  navType: Epub2Nav,
  element: Element,
  ncxDir: Path?,
): EpubTocEntry? {
  val title = element.selectFirst("navLabel > text")?.text()
  val href = element.selectFirst("content")?.attr("src")?.let { UriUtils.decode(it, Charsets.UTF_8) }
  val children = element.select(":root > ${navType.level2}").toList().mapNotNull { ncxElementToTocEntry(navType, it, ncxDir) }
  if (title != null) return EpubTocEntry(title, href?.let { normalizeHref(ncxDir, it) }, children)
  return null
}
