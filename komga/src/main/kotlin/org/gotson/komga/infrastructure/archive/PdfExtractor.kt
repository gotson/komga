package org.gotson.komga.infrastructure.archive

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.gotson.komga.domain.model.BookPage
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

@Service
class PdfExtractor : ArchiveExtractor() {

  private val mediaType = "image/jpeg"
  private val imageIOFormat = "jpeg"
  private val resolution = 1536F

  override fun getPagesList(path: Path): List<BookPage> =
      PDDocument.load(Files.newInputStream(path)).use { pdf ->
        (0..pdf.numberOfPages).map { index ->
          BookPage(index.toString(), mediaType)
        }
      }

  override fun getPageStream(path: Path, entryName: String): ByteArray =
      PDDocument.load(Files.newInputStream(path)).use { pdf ->
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