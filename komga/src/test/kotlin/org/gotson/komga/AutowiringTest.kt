package org.gotson.komga

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AutowiringTest(
  @Autowired private val dslContexts: List<DSLContext>,
) {
  @Test
  fun `Application loads properly with test properties`() = Unit

  @Test
  fun `Application has 4 dsl contexts`() {
    assertThat(dslContexts).hasSize(4)
  }
}
