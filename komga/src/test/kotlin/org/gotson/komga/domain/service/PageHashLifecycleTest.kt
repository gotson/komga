package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.PageHashKnown
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class PageHashLifecycleTest(
  @Autowired private val pageHashLifecycle: PageHashLifecycle,
) {

  @Test
  fun `given a page hash without size and action DELETE_AUTO when creating it then IllegalArgumentException is thrown`() {
    // given
    val pageHash = PageHashKnown(
      hash = "abcdef",
      mediaType = "image/jpeg",
      size = null,
      action = PageHashKnown.Action.DELETE_AUTO,
    )

    // when
    val thrown = catchThrowable { pageHashLifecycle.createOrUpdate(pageHash) }

    // then
    assertThat(thrown).isExactlyInstanceOf(IllegalArgumentException::class.java)
  }
}
