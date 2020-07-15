package org.gotson.komga.infrastructure.mediacontainer

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.gotson.komga.domain.model.MediaContainerEntry
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO

@Service
class PdfExtractor : MediaContainerExtractor {

  private val mediaType = "image/jpeg"
  private val imageIOFormat = "jpeg"
  private val resolution = 1536F

  override fun mediaTypes(): List<String> = listOf("application/pdf")

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    PDDocument.load(path.toFile()).use { pdf ->
      (0 until pdf.numberOfPages).map { index ->
        MediaContainerEntry(index.toString(), mediaType)
      }
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    PDDocument.load(path.toFile()).use { pdf ->
      val pageNumber = entryName.toInt()
      val page = pdf.getPage(pageNumber)
      val scale = resolution / minOf(page.cropBox.width, page.cropBox.height)
      val image = PDFRenderer(pdf).renderImage(pageNumber, scale, ImageType.RGB)
      ByteArrayOutputStream().use { out ->
        ImageIO.write(image, imageIOFormat, out)
        out.toByteArray()
      }
    }
}
