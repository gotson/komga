package org.gotson.komga.infrastructure.archive

import mu.KotlinLogging
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern
import org.apache.pdfbox.pdmodel.graphics.image.PDImage
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern
import org.apache.pdfbox.util.Matrix
import org.apache.pdfbox.util.Vector
import org.apache.tika.config.TikaConfig
import org.gotson.komga.domain.model.BookPage
import org.springframework.stereotype.Service
import java.awt.geom.Point2D
import java.io.ByteArrayOutputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

/**
 * Largely inspired by https://github.com/apache/pdfbox/blob/trunk/tools/src/main/java/org/apache/pdfbox/tools/ExtractImages.java
 */
@Service
class PdfExtractor(
    private val tika: TikaConfig
) : ArchiveExtractor() {

  private val JPEG = listOf(
      COSName.DCT_DECODE.name,
      COSName.DCT_DECODE_ABBREVIATION.name
  )
  private val directJPEG = true

  override fun getPagesList(path: Path): List<BookPage> {
    return PDDocument.load(Files.newInputStream(path)).use { pdf ->
      //      val xobjs = pdf.pages.flatMap { page ->
//        page.resources.xObjectNames.map { cos ->
//          cos.name to page.resources.getXObject(cos)
//        }
//      }.toMap()
//
//      xobjs.mapNotNull { (entry, xobj) ->
//        if (xobj is PDImageXObject) {
//          BookPage(entry, "image/png")
//        } else null
//      }
      pdf.pages.mapIndexed { index, page ->
        val extractor = ImageGraphicsEngine(page, false)
        extractor.run()
        BookPage(index.toString(), extractor.mediaType)
      }
    }
  }

  override fun getPageStream(path: Path, entryName: String): ByteArray {
//    return PDDocument.load(Files.newInputStream(path)).use { pdf ->
//      val xobjs = pdf.pages.flatMap { page ->
//        page.resources.xObjectNames.map { cos ->
//          cos.name to page.resources.getXObject(cos)
//        }
//      }.toMap()
//
//      val buffer = ByteArrayOutputStream()
//      ImageIO.write((xobjs[entryName] as PDImageXObject).image, "png", buffer)
//      buffer.toByteArray()
//    }
    return PDDocument.load(Files.newInputStream(path)).use { pdf ->
      val extractor = ImageGraphicsEngine(pdf.pages[entryName.toInt()], true)
      extractor.run()
      extractor.buffer
    }
  }

  private inner class ImageGraphicsEngine(
      page: PDPage,
      val extractImage: Boolean
  ) : PDFGraphicsStreamEngine(page) {

    lateinit var buffer: ByteArray
    lateinit var mediaType: String

    fun run() {
      val p = getPage()
      processPage(p)
      val res = p.resources
      for (name in res.extGStateNames) {
        val softMask = res.getExtGState(name).softMask
        if (softMask != null) {
          val group = softMask.group
          if (group != null) {
            // PDFBOX-4327: without this line NPEs will occur
//            res.getExtGState(name).copyIntoGraphicsState(graphicsState)

            processSoftMask(group)
          }
        }
      }
    }

    // find out if it is a tiling pattern, then process that one
    private fun processColor(color: PDColor) {
      if (color.colorSpace is PDPattern) {
        val pattern = color.colorSpace as PDPattern
        val abstractPattern = pattern.getPattern(color)
        if (abstractPattern is PDTilingPattern) {
          processTilingPattern(abstractPattern, null, null)
        }
      }
    }

    override fun drawImage(pdImage: PDImage) {
      if (pdImage is PDImageXObject) {
        if (pdImage.isStencil()) {
          processColor(graphicsState.nonStrokingColor)
        }
//        if (seen.contains(pdImage.cosObject)) {
//          // skip duplicate image
//          return
//        }
//        seen.add(pdImage.cosObject)
      }

      // save image
//      val name = prefix + "-" + imageCounter
//      imageCounter++

      writeToBuffer(pdImage, directJPEG, extractImage)
    }

    override fun fillAndStrokePath(windingRule: Int) {
      processColor(graphicsState.nonStrokingColor)
    }

    override fun fillPath(windingRule: Int) {
      processColor(graphicsState.nonStrokingColor)
    }

    override fun strokePath() {
      processColor(graphicsState.nonStrokingColor)
    }

    override fun showGlyph(textRenderingMatrix: Matrix,
                           font: PDFont,
                           code: Int,
                           unicode: String,
                           displacement: Vector) {
      val renderingMode = graphicsState.textState.renderingMode
      if (renderingMode.isFill) {
        processColor(graphicsState.nonStrokingColor)
      }
      if (renderingMode.isStroke) {
        processColor(graphicsState.strokingColor)
      }
    }

    override fun shadingFill(shadingName: COSName?) {}
    override fun clip(windingRule: Int) {}
    override fun endPath() {}
    override fun closePath() {}
    override fun getCurrentPoint(): Point2D = Point2D.Float(0F, 0F)
    override fun moveTo(x: Float, y: Float) {}
    override fun lineTo(x: Float, y: Float) {}
    override fun appendRectangle(p0: Point2D?, p1: Point2D?, p2: Point2D?, p3: Point2D?) {}
    override fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {}

    private fun writeToBuffer(pdImage: PDImage, directJPEG: Boolean, extractImage: Boolean) {
      var suffix: String? = pdImage.suffix
      logger.trace { "PDF image suffix: $suffix" }
      if (suffix == null || suffix == "jb2") {
        suffix = "png"
      } else if (suffix == "jpx") {
        // use jp2 suffix for file because jpx not known by windows
        suffix = "jp2"
      }
      logger.trace { "PDF image computed suffix: $suffix" }

      ByteArrayOutputStream().use { out ->
        val image = pdImage.image
        if (image != null) {
          when (suffix) {
            "jpg" -> {
              mediaType = "image/jpeg"
              val colorSpaceName = pdImage.colorSpace.name
              if (directJPEG || !hasMasks(pdImage) && (PDDeviceGray.INSTANCE.name == colorSpaceName || PDDeviceRGB.INSTANCE.name == colorSpaceName)) {
                // RGB or Gray colorspace: get and write the unmodified JPEG stream
                if (extractImage) {
                  logger.debug { "RGB or Gray colorspace, get the unmodified JPEG stream" }
                  val data = pdImage.createInputStream(JPEG)
                  IOUtils.copy(data, out)
                  IOUtils.closeQuietly(data)
                }
              } else {
                // for CMYK and other "unusual" colorspaces, the JPEG will be converted
                //            ImageIOUtil.writeImage(image, suffix, out)
                if (extractImage) {
                  logger.debug { "CMYK or other colorspace, converting to JPEG" }
                  ImageIO.write(image, suffix, out)
                }
              }
            }
            "jp2" -> {
              mediaType = "image/jp2"
              val colorSpaceName = pdImage.colorSpace.name
              if (directJPEG || !hasMasks(pdImage) && (PDDeviceGray.INSTANCE.name == colorSpaceName || PDDeviceRGB.INSTANCE.name == colorSpaceName)) {
                // RGB or Gray colorspace: get and write the unmodified JPEG2000 stream
                if (extractImage) {
                  logger.debug { "RGB or Gray colorspace, get the unmodified JPEG2000 stream" }
                  val data = pdImage.createInputStream(listOf(COSName.JPX_DECODE.name))
                  IOUtils.copy(data, out)
                  IOUtils.closeQuietly(data)
                }
              } else {
                // for CMYK and other "unusual" colorspaces, the image will be converted
                //            ImageIOUtil.writeImage(image, "jpeg2000", out)
                if (extractImage) {
                  logger.debug { "CMYK or other colorspace, converting to JPEG2000" }
                  ImageIO.write(image, "jpeg2000", out)
                }
              }
            }
            else -> {
              mediaType = URLConnection.guessContentTypeFromName("file$suffix")
              if (extractImage) {
                //          ImageIOUtil.writeImage(image, suffix, out)
                logger.debug { "Converting to $suffix" }
                ImageIO.write(image, suffix, out)
              }
            }
          }
        }
        buffer = out.toByteArray()
      }
    }

  }

  private fun hasMasks(pdImage: PDImage): Boolean {
    return if (pdImage is PDImageXObject) {
      pdImage.mask != null || pdImage.softMask != null
    } else false
  }
}