package org.gotson.komga.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.infrastructure.archive.ContentDetector
import org.gotson.komga.infrastructure.archive.PdfExtractor
import org.gotson.komga.infrastructure.archive.RarExtractor
import org.gotson.komga.infrastructure.archive.ZipExtractor
import org.junit.jupiter.api.Test

class BookParserTest {
  private val mockContent = mockk<ContentDetector>()
  private val mockZip = mockk<ZipExtractor>()
  private val mockRar = mockk<RarExtractor>()
  private val mockPDf = mockk<PdfExtractor>()

  private val bookParser = BookParser(mockContent, mockZip, mockRar, mockPDf)

  @Test
  fun `given book with unordered pages when parsing then thumbnail should always be the first in natural order`() {
    // given
    val book = makeBook("book")
    every { mockContent.detectMediaType(book.path()) } returns "application/zip"

    val unorderedPages = listOf("08", "01", "02").map { makeBookPage(it) }
    every { mockZip.getPagesList(book.path()) } returns unorderedPages

    //when
    val thumbnailFile = slot<String>()
    every { mockZip.getPageStream(book.path(), capture(thumbnailFile)) } returns ByteArray(1)
    bookParser.parse(book)

    // then
    assertThat(thumbnailFile.captured).isEqualTo("01")
  }
}
