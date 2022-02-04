package org.gotson.komga.infrastructure.mediacontainer

import com.github.benmanes.caffeine.cache.Caffeine
import mu.KotlinLogging
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Service
class PdfExtractor : MediaContainerExtractor {

  private val mediaType = "image/jpeg"
  private val imageIOFormat = "jpeg"
  private val resolution = 1536F

  private val cache = Caffeine.newBuilder()
    .maximumSize(20)
    .expireAfterAccess(1, TimeUnit.MINUTES)
    .evictionListener { _: Path?, pdf: PDDocument?, _ -> pdf?.close() }
    .build<Path, PDDocument>()

  override fun mediaTypes(): List<String> = listOf(MediaType.PDF.value)

  override fun getEntries(path: Path, analyzeDimensions: Boolean): List<MediaContainerEntry> =
    PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()).use { pdf ->
      (0 until pdf.numberOfPages).map { index ->
        val page = pdf.getPage(index)
        val scale = page.getScale()
        val dimension = if (analyzeDimensions) Dimension((page.cropBox.width * scale).roundToInt(), (page.cropBox.height * scale).roundToInt()) else null
        MediaContainerEntry(name = index.toString(), mediaType = mediaType, dimension = dimension)
      }
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray {
    val pdf = cache.get(path) { PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()) }!!
    val pageNumber = entryName.toInt()
    val page = pdf.getPage(pageNumber)
    val image = PDFRenderer(pdf).renderImage(pageNumber, page.getScale(), ImageType.RGB)
    return ByteArrayOutputStream().use { out ->
      ImageIO.write(image, imageIOFormat, out)
      out.toByteArray()
    }
  }

  private fun PDPage.getScale() = resolution / minOf(cropBox.width, cropBox.height)
}
