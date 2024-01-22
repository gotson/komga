package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PageHashTest {
  @Test
  fun `given negative size when creating a PageHash then its size is null`() {
    val pageHash = PageHash("abc", -5)

    assertThat(pageHash.size).isNull()
  }

  @Test
  fun `given null size when creating a PageHash then its size is null`() {
    val pageHash = PageHash("abc", null)

    assertThat(pageHash.size).isNull()
  }

  @Test
  fun `given size when creating a PageHash then its size is not null`() {
    val pageHash = PageHash("abc", 5)

    assertThat(pageHash.size).isNotNull
  }
}
