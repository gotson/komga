package org.gotson.komga.infrastructure.datasource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DatabaseMigrationTest {

  companion object {
    @JvmStatic
    fun h2Urls() =
      listOf(
        "not a jdbc url" to null,
        "jdbc:h2:./testdb" to "./testdb",
        "jdbc:h2:file:./testdb" to "./testdb",
        "jdbc:h2:~/.komga/database.h2" to "~/.komga/database.h2",
        "jdbc:h2:mem:testdb" to null,
        "jdbc:h2:tcp://localhost/~/test" to null,
        "jdbc:h2:ssl://localhost:8085/~/sample" to null,
        "jdbc:h2:file:~/private;CIPHER=AES;FILE_LOCK=SOCKET" to "~/private",
        "jdbc:h2:zip:~/db.zip!/test" to null
      )
  }

  @ParameterizedTest
  @MethodSource("h2Urls")
  fun `given h2 url when extracting file name then file name is returned`(pair: Pair<String, String?>) {
    val fileName = extractH2Path(pair.first)
    assertThat(fileName).isEqualTo(pair.second)
  }
}
