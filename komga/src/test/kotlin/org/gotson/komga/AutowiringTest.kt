package org.gotson.komga

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.sql.DataSource

@SpringBootTest
class AutowiringTest(
  @Autowired private val dataSources: List<DataSource>,
  @Autowired private val dslContexts: List<DSLContext>,
) {
  @Test
  fun `Application loads properly with test properties`() = Unit

  @Test
  fun `Application has 4 dsl contexts`() {
    assertThat(dataSources).hasSize(4)
    assertThat(dslContexts).hasSize(4)
  }
}
