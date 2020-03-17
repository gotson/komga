package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class AuthorTest {
  @Test
  fun `given blank parameters when creating object then IllegalArgumentException is thrown`() {
    val blankName = catchThrowable { Author(name = "", role = "role") }
    val blankRole = catchThrowable { Author(name = "name", role = "") }

    assertThat(blankName).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankRole).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given blank parameters when setting fields then IllegalArgumentException is thrown`() {
    val author = Author(name = "name", role = "role")

    val blankName = catchThrowable { author.name = " " }
    val blankRole = catchThrowable { author.role = " " }

    assertThat(blankName).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(blankRole).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given untrimmed parameters when creating object then fields are trimmed and role is lowercase`() {
    val author = Author(name = "  name  ", role = "  Role  ")

    assertThat(author.name).isEqualTo("name")
    assertThat(author.role).isEqualTo("role")
  }

  @Test
  fun `given untrimmed parameters when setting fields then fields are trimmed and role is lowercase`() {
    val author = Author(name = "name", role = "role")

    author.name = "  setName  "
    author.role = "  set Role  "

    assertThat(author.name).isEqualTo("setName")
    assertThat(author.role).isEqualTo("set role")
  }
}
