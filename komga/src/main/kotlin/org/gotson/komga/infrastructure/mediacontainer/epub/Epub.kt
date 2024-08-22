package org.gotson.komga.infrastructure.mediacontainer.epub

import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.infrastructure.util.use
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.nio.file.Path
import java.nio.file.Paths

data class EpubPackage(
  val zip: ZipFile,
  val opfDoc: Document,
  val opfDir: Path?,
  val manifest: Map<String, ManifestItem>,
)

inline fun <R> Path.epub(block: (EpubPackage) -> R): R =
  ZipFile.builder().setPath(this).use { zip ->
    val opfFile = zip.getPackagePath()
    val opfDoc = zip.getInputStream(zip.getEntry(opfFile)).use { Jsoup.parse(it, null, "", Parser.xmlParser()) }
    val opfDir = Paths.get(opfFile).parent
    block(EpubPackage(zip, opfDoc, opfDir, opfDoc.getManifest()))
  }

fun ZipFile.getPackagePath(): String =
  getEntry("META-INF/container.xml").let { entry ->
    val container = getInputStream(entry).use { Jsoup.parse(it, null, "") }
    container.getElementsByTag("rootfile").first()?.attr("full-path") ?: throw MediaUnsupportedException("META-INF/container.xml does not contain rootfile tag")
  }

fun getPackageFile(path: Path): String? =
  ZipFile.builder().setPath(path).use { zip ->
    try {
      zip.getInputStream(zip.getEntry(zip.getPackagePath())).reader().use { it.readText() }
    } catch (e: Exception) {
      null
    }
  }
