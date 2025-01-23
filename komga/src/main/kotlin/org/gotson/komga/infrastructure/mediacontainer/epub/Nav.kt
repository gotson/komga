package org.gotson.komga.infrastructure.mediacontainer.epub

import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.infrastructure.util.getEntryBytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.web.util.UriUtils
import java.nio.file.Path
import kotlin.io.path.Path

fun EpubPackage.getNavResource(): ResourceContent? =
  manifest.values.firstOrNull { it.properties.contains("nav") }?.let { nav ->
    val href = normalizeHref(opfDir, nav.href)
    zip.getEntryBytes(href)?.decodeToString()?.let { navContent ->
      ResourceContent(Path(href), navContent)
    }
  }

fun processNav(
  document: ResourceContent,
  navElement: Epub3Nav,
): List<EpubTocEntry> {
  val doc = Jsoup.parse(document.content)
  val nav =
    doc
      .select("nav")
      // Jsoup selectors cannot find an attribute with namespace
      .firstOrNull { it.attributes().any { attr -> attr.key.endsWith("type") && attr.value == navElement.value } }
  return nav?.select(":root > ol > li")?.toList()?.mapNotNull { navLiElementToTocEntry(it, document.path.parent) } ?: emptyList()
}

private fun navLiElementToTocEntry(
  element: Element,
  navDir: Path?,
): EpubTocEntry? {
  val title = element.selectFirst(":root > a, span")?.text()
  val href = element.selectFirst(":root > a")?.attr("href")?.let { UriUtils.decode(it, Charsets.UTF_8) }
  val children = element.select(":root > ol > li").mapNotNull { navLiElementToTocEntry(it, navDir) }
  if (title != null) return EpubTocEntry(title, href?.let { normalizeHref(navDir, it) }, children)
  return null
}
