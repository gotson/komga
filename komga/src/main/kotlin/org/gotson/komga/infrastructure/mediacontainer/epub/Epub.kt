package org.gotson.komga.infrastructure.mediacontainer.epub

import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.infrastructure.util.getEntryBytes
import org.gotson.komga.infrastructure.util.getEntryInputStream
import org.gotson.komga.infrastructure.util.use
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

data class EpubPackage(
  val path: Path,
  val zip: ZipFile,
  val opfDoc: Document,
  val opfDir: Path?,
  val manifest: Map<String, ManifestItem>,
  private val entryDecryptor: (String, ByteArray) -> ByteArray = { _, bytes -> bytes },
) {
  fun getEntryBytes(entryName: String): ByteArray? = zip.getEntryBytes(entryName)?.let { entryDecryptor(entryName, it) }

  fun getEntryInputStream(entryName: String): InputStream? = getEntryBytes(entryName)?.inputStream()
}

inline fun <R> Path.epub(
  noinline entryDecryptor: (String, ByteArray) -> ByteArray = { _, bytes -> bytes },
  block: (EpubPackage) -> R,
): R =
  ZipFile.builder().setPath(this).use { zip ->
    val opfFile = zip.getPackagePath()
    val opfDoc = zip.getEntryInputStream(opfFile)?.use { Jsoup.parse(it, null, "", Parser.xmlParser()) } ?: throw MediaUnsupportedException("Could not open OPF resource")
    val opfDir = Paths.get(opfFile).parent
    block(EpubPackage(this, zip, opfDoc, opfDir, opfDoc.getManifest(), entryDecryptor))
  }

/**
 * Returns the zip entry path of the Epub package file
 */
fun ZipFile.getPackagePath(): String =
  getEntryInputStream("META-INF/container.xml")
    ?.use { Jsoup.parse(it, null, "", Parser.xmlParser()) }
    ?.getElementsByTag("rootfile")
    ?.first()
    ?.attr("full-path") ?: throw MediaUnsupportedException("META-INF/container.xml does not contain rootfile tag")

/**
 * Returns the content of the Epub package file as a [String]
 */
fun readPackageFileContent(path: Path): String? =
  ZipFile.builder().setPath(path).use { zip ->
    try {
      zip.getEntryInputStream(zip.getPackagePath())?.reader()?.use { it.readText() }
    } catch (e: Exception) {
      null
    }
  }
