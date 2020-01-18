package org.gotson.komga.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.PdfExtractor
import org.gotson.komga.infrastructure.mediacontainer.RarExtractor
import org.gotson.komga.infrastructure.mediacontainer.ZipExtractor
import org.junit.jupiter.api.Test

class BookAnalyzerTest {
  private val mockContent = mockk<ContentDetector>()
  private val mockZip = mockk<ZipExtractor>()
  private val mockRar = mockk<RarExtractor>()
  private val mockPDf = mockk<PdfExtractor>()
  private val mockImageConverter = mockk<ImageConverter>()

  private val bookAnalyzer = BookAnalyzer(mockContent, mockZip, mockRar, mockPDf, mockImageConverter)

  @Test
  fun `given book with unordered pages when analyzing then thumbnail should always be the first in natural order`() {
    // given
    val book = makeBook("book")
    every { mockContent.detectMediaType(book.path()) } returns "application/zip"
    every { mockContent.isImage(any()) } returns true

    val unorderedPages = listOf("08", "01", "02").map { MediaContainerEntry(it, "image/png") }
    every { mockZip.getEntries(book.path()) } returns unorderedPages

    //when
    val thumbnailFile = slot<String>()
    every { mockZip.getEntryStream(book.path(), capture(thumbnailFile)) } returns ByteArray(1)
    bookAnalyzer.analyze(book)

    // then
    assertThat(thumbnailFile.captured).isEqualTo("01")
  }
}
