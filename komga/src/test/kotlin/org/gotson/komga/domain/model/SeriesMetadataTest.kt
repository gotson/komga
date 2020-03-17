package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class SeriesMetadataTest {
  @Test
  fun `given blank parameters when creating object then IllegalArgumentException is thrown`() {
    val blankTitle = catchThrowable { SeriesMetadata(title = "", titleSort = "title") }
    val blankTitleSort = catchThrowable { SeriesMetadata(title = "title", titleSort = "") }

    assertThat(blankTitle).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankTitleSort).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given blank parameters when setting fields then IllegalArgumentException is thrown`() {
    val metadata = SeriesMetadata(title = "title")

    val blankTitle = catchThrowable { metadata.title = "" }
    val blankTitleSort = catchThrowable { metadata.titleSort = "" }

    assertThat(blankTitle).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankTitleSort).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed`() {
    val metadata = SeriesMetadata(title = "  title  ", titleSort = "  titleSort  ")

    assertThat(metadata.title).isEqualTo("title")
    assertThat(metadata.titleSort).isEqualTo("titleSort")
  }

  @Test
  fun `given untrimmed parameters when setting fields then fields are trimmed`() {
    val metadata = SeriesMetadata(title = "title")

    metadata.title = "  setTitle  "
    metadata.titleSort = "  setTitleSort  "

    assertThat(metadata.title).isEqualTo("setTitle")
    assertThat(metadata.titleSort).isEqualTo("setTitleSort")
  }
}
