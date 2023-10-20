package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PageExtractor
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Service
class PdfExtractor : MediaContainerRawExtractor {

  private val mediaType = "image/jpeg"
  private val imageIOFormat = "jpeg"
  private val resolution = 1536F

  override fun mediaTypes(): List<String> = listOf(MediaType.PDF.type)

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
    PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()).use { pdf ->
      val pageNumber = entryName.toInt()
      val page = pdf.getPage(pageNumber)
      val image = PDFRenderer(pdf).renderImage(pageNumber, page.getScale(), ImageType.RGB)
      return ByteArrayOutputStream().use { out ->
        ImageIO.write(image, imageIOFormat, out)
        out.toByteArray()
      }
    }
  }

  override fun getRawEntryStream(path: Path, entryName: String): BookPageContent {
    PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()).use { pdf ->
      val pageNumber = entryName.toInt() + 1
      val bytes = ByteArrayOutputStream().use { out ->
        PageExtractor(pdf, pageNumber, pageNumber).extract().save(out)
        out.toByteArray()
      }
      return BookPageContent(bytes, MediaType.PDF.type)
    }
  }

  private fun PDPage.getScale() = resolution / minOf(cropBox.width, cropBox.height)
}
