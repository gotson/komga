package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.infrastructure.metadata.comicrack.dto.Book
import org.gotson.komga.infrastructure.metadata.comicrack.dto.ReadingList
import org.junit.jupiter.api.Test

class ReadListProviderTest {

  private val mockMapper = mockk<XmlMapper>()
  private val readListProvider = ReadListProvider(mockMapper)

  @Test
  fun `given CBL list with books when getting ReadListRequest then it is valid`() {
    // given
    val cbl = ReadingList().apply {
      name = "my read list"
      books = listOf(
        Book().apply {
          series = "series 1"
          number = " 4 "
          volume = 2005
        },
        Book().apply {
          series = "series 2"
          number = "1"
        },
      )
    }

    every { mockMapper.readValue(any<ByteArray>(), ReadingList::class.java) } returns cbl

    // when
    val request = readListProvider.importFromCbl(ByteArray(0))

    // then
    assertThat(request).isNotNull
    with(request!!) {
      assertThat(name).isEqualTo(cbl.name)
      assertThat(books).hasSize(2)

      with(books[0]) {
        assertThat(series).isEqualTo("series 1 (2005)")
        assertThat(number).isEqualTo("4")
      }

      with(books[1]) {
        assertThat(series).isEqualTo("series 2")
        assertThat(number).isEqualTo("1")
      }
    }
  }

  @Test
  fun `given CBL list with invalid books when getting ReadListRequest then it is null`() {
    // given
    val cbl = ReadingList().apply {
      name = "my read list"
      books = listOf(
        Book().apply {
          series = " "
          number = "4"
          volume = 2005
        },
        Book().apply {
          series = null
          number = "1"
        },
        Book().apply {
          series = "Series"
          number = null
        },
      )
    }

    every { mockMapper.readValue(any<ByteArray>(), ReadingList::class.java) } returns cbl

    // when
    val request = readListProvider.importFromCbl(ByteArray(0))

    // then
    assertThat(request).isNull()
  }

  @Test
  fun `given CBL list without books when getting ReadListRequest then it is null`() {
    // given
    val cbl = ReadingList().apply {
      name = "my read list"
      books = emptyList()
    }

    every { mockMapper.readValue(any<ByteArray>(), ReadingList::class.java) } returns cbl

    // when
    val request = readListProvider.importFromCbl(ByteArray(0))

    // then
    assertThat(request).isNull()
  }

  @Test
  fun `given CBL list without name when getting ReadListRequest then it is null`() {
    // given
    val cbl = ReadingList().apply {
      name = null
      books = listOf(
        Book().apply {
          series = "series 1"
          number = "4"
          volume = 2005
        },
        Book().apply {
          series = "series 2"
          number = "1"
        },
      )
    }

    every { mockMapper.readValue(any<ByteArray>(), ReadingList::class.java) } returns cbl

    // when
    val request = readListProvider.importFromCbl(ByteArray(0))

    // then
    assertThat(request).isNull()
  }

  @Test
  fun `given CBL list with blank name when getting ReadListRequest then it is null`() {
    // given
    val cbl = ReadingList().apply {
      name = "  "
      books = listOf(
        Book().apply {
          series = "series 1"
          number = "4"
          volume = 2005
        },
        Book().apply {
          series = "series 2"
          number = "1"
        },
      )
    }

    every { mockMapper.readValue(any<ByteArray>(), ReadingList::class.java) } returns cbl

    // when
    val request = readListProvider.importFromCbl(ByteArray(0))

    // then
    assertThat(request).isNull()
  }
}
