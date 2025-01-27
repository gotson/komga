package org.gotson.komga.infrastructure.mediacontainer.epub

import org.gotson.komga.domain.model.EpubTocEntry
import org.jsoup.nodes.Document
import org.springframework.web.util.UriUtils
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString

fun Document.getManifest() =
  select("manifest > item").associate {
    it.attr("id") to
      ManifestItem(
        it.attr("id"),
        it.attr("href"),
        it.attr("media-type"),
        it.attr("properties").split(" ").toSet(),
      )
  }

fun normalizeHref(
  opfDir: Path?,
  href: String,
) = (opfDir?.resolve(href)?.normalize() ?: Paths.get(href)).invariantSeparatorsPathString

/**
 * Process an OPF document and extracts TOC entries
 * from the <guide> section.
 */
fun processOpfGuide(
  opf: Document,
  opfDir: Path?,
): List<EpubTocEntry> {
  val guide = opf.selectFirst("guide") ?: return emptyList()
  return guide.select("reference").map { ref ->
    EpubTocEntry(
      ref.attr("title"),
      ref.attr("href").ifBlank { null }?.let { normalizeHref(opfDir, UriUtils.decode(it, Charsets.UTF_8)) },
    )
  }
}
