package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class BookMetadataTest {
  @Test
  fun `given blank parameters when creating object then IllegalArgumentException is thrown`() {
    val blankTitle = catchThrowable { BookMetadata(title = "", number = "1", numberSort = 1F) }
    val blankNumber = catchThrowable { BookMetadata(title = "title", number = "", numberSort = 1F) }

    assertThat(blankTitle).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankNumber).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given blank parameters when setting fields then IllegalArgumentException is thrown`() {
    val metadata = BookMetadata(title = "title", number = "1", numberSort = 1F)

    val blankTitle = catchThrowable { metadata.title = "" }
    val blankNumber = catchThrowable { metadata.number = "" }


    assertThat(blankTitle).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankNumber).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed`() {
    val metadata = BookMetadata(title = "  title  ", number = "  number  ", numberSort = 1F)

    assertThat(metadata.title).isEqualTo("title")
    assertThat(metadata.number).isEqualTo("number")
  }

  @Test
  fun `given untrimmed parameters when setting fields then fields are trimmed`() {
    val metadata = BookMetadata(title = "title", number = "number", numberSort = 1F)

    metadata.title = "  setTitle  "
    metadata.number = "  setNumber  "

    assertThat(metadata.title).isEqualTo("setTitle")
    assertThat(metadata.number).isEqualTo("setNumber")
  }
}
