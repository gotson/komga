package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class ReadingListTest {

  private val mapper = XmlMapper()

  @Test
  fun `given valid xml file when deserializing then properties are available`() {
    val file = ClassPathResource("comicrack/ReadingList.xml")
    val readingList = mapper.readValue<ReadingList>(file.url)

    with(readingList) {
      assertThat(name).isEqualTo("Civil War")
      assertThat(books).hasSize(3)

      with(books[0]) {
        assertThat(series).isEqualTo("Civil War")
        assertThat(number).isEqualTo("1")
        assertThat(volume).isEqualTo(2006)
        assertThat(year).isEqualTo(2006)
        assertThat(fileName).isEqualTo("Civil War Vol.2006 #01 (July, 2006)")
      }

      with(books[1]) {
        assertThat(series).isEqualTo("Wolverine")
        assertThat(number).isEqualTo("42")
        assertThat(volume).isEqualTo(2003)
        assertThat(year).isEqualTo(2006)
        assertThat(fileName).isEqualTo("Wolverine Vol.2003 #42 (July, 2006)")
      }

      with(books[2]) {
        assertThat(series).isEqualTo("X-Factor")
        assertThat(number).isEqualTo("HS")
        assertThat(volume).isEqualTo(2006)
        assertThat(year).isEqualTo(2006)
        assertThat(fileName).isEqualTo("X-Factor Vol.2006 #08 (August, 2006)")
      }
    }
  }

  @Test
  fun `given valid xml file for smart list when deserializing then properties are available`() {
    val file = ClassPathResource("comicrack/ReadingList_SmartList.xml")
    val mapper = XmlMapper()
    val readingList = mapper.readValue<ReadingList>(file.url)

    with(readingList) {
      assertThat(name).isEqualTo("Golden Age")
      assertThat(books).isEmpty()
    }
  }
}
