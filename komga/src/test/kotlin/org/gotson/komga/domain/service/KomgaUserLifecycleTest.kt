package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.security.apikey.ApiKeyGenerator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KomgaUserLifecycleTest(
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
) {
  @SpykBean
  private lateinit var apiKeyGenerator: ApiKeyGenerator

  private val user1 = KomgaUser("user1@example.org", "")
  private val user2 = KomgaUser("user2@example.org", "")

  @BeforeAll
  fun setup() {
    userRepository.insert(user1)
    userRepository.insert(user2)
  }

  @AfterEach
  fun cleanup() {
    userRepository.deleteApiKeyByUserId(user1.id)
    userRepository.deleteApiKeyByUserId(user2.id)
  }

  @AfterAll
  fun teardown() {
    userRepository.deleteAll()
  }

  @Test
  fun `given existing api key when api key cannot be uniquely generated then it returns null`() {
    // given
    val uuid = ApiKeyGenerator().generate()
    every { apiKeyGenerator.generate() } returns uuid
    userLifecycle.createApiKey(user1, "test key")

    // when
    val apiKey = userLifecycle.createApiKey(user1, "test key 2")
    val apiKey2 = userLifecycle.createApiKey(user2, "test key 3")

    // then
    assertThat(apiKey).isNull()
    assertThat(apiKey2).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["test", "TEST", " test "])
  fun `given existing api key comment when api key with same comment is generated then it throws exception`(comment: String) {
    // given
    userLifecycle.createApiKey(user1, "test")

    // when
    val thrown = catchThrowable { userLifecycle.createApiKey(user1, comment) }

    // then
    assertThat(thrown).isExactlyInstanceOf(DuplicateNameException::class.java)
  }
}
