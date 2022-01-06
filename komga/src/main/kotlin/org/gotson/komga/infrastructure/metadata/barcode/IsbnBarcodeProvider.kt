package org.gotson.komga.infrastructure.metadata.barcode

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import mu.KotlinLogging
import org.apache.commons.validator.routines.ISBNValidator
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.springframework.stereotype.Service
import java.util.EnumSet
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

private const val PAGES_LAST = 3
private const val PAGES_FIRST = 3

@Service
class IsbnBarcodeProvider(
  private val bookAnalyzer: BookAnalyzer,
  private val validator: ISBNValidator,
) : BookMetadataProvider {

  private val hints = mapOf(
    DecodeHintType.POSSIBLE_FORMATS to EnumSet.of(BarcodeFormat.EAN_13),
    DecodeHintType.TRY_HARDER to true,
  )

  override fun getCapabilities(): Set<BookMetadataPatchCapability> =
    setOf(BookMetadataPatchCapability.ISBN)

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    val pagesToTry = (1..book.media.pages.size).toList().let {
      (it.takeLast(PAGES_LAST).reversed() + it.take(PAGES_FIRST)).distinct()
    }

    for (p in pagesToTry) {
      val imageBytes = bookAnalyzer.getPageContent(book, p)
      ImageIO.read(imageBytes.inputStream())?.let { image ->
        val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val source = RGBLuminanceSource(image.width, image.height, pixels)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val result = try {
          MultiFormatReader().decode(bitmap, hints)
        } catch (e: Exception) {
          null
        }

        if (result == null || result.text == null) {
          logger.debug { "Book page $p does not contain a barcode: $book" }
        } else {
          if (validator.isValid(result.text)) {
            logger.debug { "Book page $p contains barcode which is valid ISBN: '${result.text}'. $book" }
            return BookMetadataPatch(isbn = validator.validate(result.text))
          } else {
            logger.debug { "Book page $p contains barcode which is invalid ISBN: '${result.text}'. $book" }
          }
        }
      }
    }

    return null
  }
}
