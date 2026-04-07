package org.gotson.komga.infrastructure.datasource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

@Testcontainers
@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class PostgreSQLIntegrationTest {
  companion object {
    @Container
    val postgres =
      PostgreSQLContainer("postgres:16-alpine")
        .withDatabaseName("komga_test")
        .withUsername("test")
        .withPassword("test")

    @DynamicPropertySource
    @JvmStatic
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("komga.database.type") { "postgresql" }
      registry.add("komga.database.url") { postgres.jdbcUrl }
      registry.add("komga.database.username") { postgres.username }
      registry.add("komga.database.password") { postgres.password }
    }
  }

  @Autowired
  private lateinit var dataSource: DataSource

  @Autowired
  private lateinit var databaseUdfProvider: DatabaseUdfProvider

  @Test
  fun `should connect to PostgreSQL database`() {
    val connection = dataSource.connection
    assertThat(connection.isValid(2)).isTrue()
    connection.close()
  }

  @Test
  fun `should use PostgreSQL UDF provider`() {
    assertThat(databaseUdfProvider).isInstanceOf(PostgresUdfProvider::class.java)
  }
}
