package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Service
class PdfExtractor(
  private val imageAnalyzer: ImageAnalyzer
) : MediaContainerExtractor {

  private val mediaType = "image/jpeg"
  private val imageIOFormat = "jpeg"
  private val resolution = 1536F

  override fun mediaTypes(): List<String> = listOf("application/pdf")

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    PDDocument.load(path.toFile()).use { pdf ->
      (0 until pdf.numberOfPages).map { index ->
        val page = pdf.getPage(index)
        val scale = page.getScale()
        val dimension = Dimension((page.cropBox.width * scale).roundToInt(), (page.cropBox.height * scale).roundToInt())
        MediaContainerEntry(name = index.toString(), mediaType = mediaType, dimension = dimension)
      }
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    PDDocument.load(path.toFile()).use { pdf ->
      val pageNumber = entryName.toInt()
      val page = pdf.getPage(pageNumber)
      val image = PDFRenderer(pdf).renderImage(pageNumber, page.getScale(), ImageType.RGB)
      ByteArrayOutputStream().use { out ->
        ImageIO.write(image, imageIOFormat, out)
        out.toByteArray()
      }
    }

  private fun PDPage.getScale() = resolution / minOf(cropBox.width, cropBox.height)
}
