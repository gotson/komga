package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthorTest {
  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed and role is lowercase`() {
    val author = Author(name = "  name  ", role = "  Role  ")

    assertThat(author.name).isEqualTo("name")
    assertThat(author.role).isEqualTo("role")
  }
}
