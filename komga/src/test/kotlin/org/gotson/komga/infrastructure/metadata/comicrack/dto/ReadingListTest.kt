package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReadingListTest {
  private val mapper = XmlMapper()

  @Test
  fun `given valid xml file when deserializing then properties are available`() {
    // language=XML
    val cbl =
      """
      <?xml version="1.0"?>
      <ReadingList>
        <Name>Civil War</Name>
        <Books>
          <Book Series="Civil War" Number="1" Volume="2006" Year="2006">
            <Id>1b21c8c4-e8d7-44f6-992d-97d24ce8123e</Id>
            <FileName>Civil War Vol.2006 #01 (July, 2006)</FileName>
          </Book>
          <Book Series="Wolverine" Number="42" Volume="2003" Year="2006">
            <Id>29a69cbf-af64-471d-889c-8fc0f0080f7c</Id>
            <FileName>Wolverine Vol.2003 #42 (July, 2006)</FileName>
          </Book>
          <Book Series="X-Factor" Number="HS" Volume="2006" Year="2006">
            <Id>ec70e585-5a80-428c-a67e-fd22b668449b</Id>
            <FileName>X-Factor Vol.2006 #08 (August, 2006)</FileName>
          </Book>
        </Books>
        <Matchers />
      </ReadingList>
      """.trimIndent()
    val readingList = mapper.readValue<ReadingList>(cbl)

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
    // language=XML
    val cbl =
      """
      <?xml version="1.0"?>
      <ReadingList xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <Name>Golden Age</Name>
        <Books />
        <Matchers>
          <ComicBookMatcher xsi:type="ComicBookSeriesMatcher" MatchOperator="4">
            <MatchValue>Marvel Masterworks</MatchValue>
          </ComicBookMatcher>
          <ComicBookMatcher xsi:type="ComicBookSeriesMatcher" MatchOperator="1">
            <MatchValue>Golden Age</MatchValue>
          </ComicBookMatcher>
        </Matchers>
      </ReadingList>
      """.trimIndent()
    val readingList = mapper.readValue<ReadingList>(cbl)

    with(readingList) {
      assertThat(name).isEqualTo("Golden Age")
      assertThat(books).isEmpty()
    }
  }
}
