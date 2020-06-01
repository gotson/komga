package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SeriesMetadataTest {
  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed`() {
    val metadata = SeriesMetadata(title = "  title  ", titleSort = "  titleSort  ")

    assertThat(metadata.title).isEqualTo("title")
    assertThat(metadata.titleSort).isEqualTo("titleSort")
  }
}
