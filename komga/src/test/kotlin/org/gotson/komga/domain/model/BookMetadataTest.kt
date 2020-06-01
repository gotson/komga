package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BookMetadataTest {
  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed`() {
    val metadata = BookMetadata(title = "  title  ", number = "  number  ", numberSort = 1F)

    assertThat(metadata.title).isEqualTo("title")
    assertThat(metadata.number).isEqualTo("number")
  }
}
