package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.infrastructure.jooq.offset
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class KomgaUserDaoTest(
  @Autowired private val komgaUserDao: KomgaUserDao,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val library = makeLibrary()

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
  }

  @AfterEach
  fun deleteUsers() {
    komgaUserDao.deleteAll()
    assertThat(komgaUserDao.count()).isEqualTo(0)
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a user when saving it then it is persisted`() {
    val now = LocalDateTime.now()
    val user =
      KomgaUser(
        email = "user@example.org",
        password = "password",
        sharedLibrariesIds = setOf(library.id),
        sharedAllLibraries = false,
      )

    komgaUserDao.insert(user)
    val created = komgaUserDao.findByIdOrNull(user.id)!!

    with(created) {
      assertThat(id).isNotEqualTo(0)
      assertThat(createdDate).isCloseTo(now, offset)
      assertThat(lastModifiedDate).isCloseTo(now, offset)
      assertThat(email).isEqualTo("user@example.org")
      assertThat(password).isEqualTo("password")
      assertThat(roles).containsExactlyInAnyOrder(UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING)
      assertThat(sharedLibrariesIds).containsExactly(library.id)
      assertThat(sharedAllLibraries).isFalse
      assertThat(restrictions.ageRestriction).isNull()
      assertThat(restrictions.labelsAllow).isEmpty()
      assertThat(restrictions.labelsExclude).isEmpty()
    }
  }

  @Test
  fun `given existing user when modifying and saving it then it is persisted`() {
    val user =
      KomgaUser(
        email = "user@example.org",
        password = "password",
        sharedLibrariesIds = setOf(library.id),
        sharedAllLibraries = false,
        restrictions =
          ContentRestrictions(
            ageRestriction = AgeRestriction(10, AllowExclude.ALLOW_ONLY),
            labelsAllow = setOf("allow"),
            labelsExclude = setOf("exclude"),
          ),
      )

    komgaUserDao.insert(user)
    val created = komgaUserDao.findByIdOrNull(user.id)!!
    with(created) {
      assertThat(restrictions.ageRestriction).isNotNull
      assertThat(restrictions.ageRestriction!!.age).isEqualTo(10)
      assertThat(restrictions.ageRestriction!!.restriction).isEqualTo(AllowExclude.ALLOW_ONLY)
      assertThat(restrictions.labelsAllow).containsExactly("allow")
      assertThat(restrictions.labelsExclude).containsExactly("exclude")
    }

    val modified =
      created.copy(
        email = "user2@example.org",
        password = "password2",
        roles = setOf(UserRoles.ADMIN, UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING, UserRoles.KOBO_SYNC),
        sharedLibrariesIds = emptySet(),
        sharedAllLibraries = true,
        restrictions =
          ContentRestrictions(
            ageRestriction = AgeRestriction(16, AllowExclude.EXCLUDE),
            labelsAllow = setOf("allow2"),
            labelsExclude = setOf("exclude2"),
          ),
      )
    val modifiedDate = LocalDateTime.now()
    komgaUserDao.update(modified)
    val modifiedSaved = komgaUserDao.findByIdOrNull(modified.id)!!

    with(modifiedSaved) {
      assertThat(id).isEqualTo(created.id)
      assertThat(createdDate).isEqualTo(created.createdDate)
      assertThat(lastModifiedDate)
        .isCloseTo(modifiedDate, offset)
        .isNotEqualTo(modified.createdDate)
      assertThat(email).isEqualTo("user2@example.org")
      assertThat(password).isEqualTo("password2")
      assertThat(roles).containsExactlyInAnyOrder(UserRoles.ADMIN, UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING, UserRoles.KOBO_SYNC)
      assertThat(sharedLibrariesIds).isEmpty()
      assertThat(sharedAllLibraries).isTrue
      assertThat(restrictions.ageRestriction).isNotNull
      assertThat(restrictions.ageRestriction!!.age).isEqualTo(16)
      assertThat(restrictions.ageRestriction!!.restriction).isEqualTo(AllowExclude.EXCLUDE)
      assertThat(restrictions.labelsAllow).containsExactly("allow2")
      assertThat(restrictions.labelsExclude).containsExactly("exclude2")
    }

    komgaUserDao.update(modifiedSaved.copy(restrictions = ContentRestrictions()))
    with(komgaUserDao.findByIdOrNull(modified.id)!!) {
      assertThat(restrictions.ageRestriction).isNull()
      assertThat(restrictions.labelsAllow).isEmpty()
      assertThat(restrictions.labelsExclude).isEmpty()
    }
  }

  @Test
  fun `given multiple users when saving then they are persisted`() {
    komgaUserDao.insert(KomgaUser("user1@example.org", "p"))
    komgaUserDao.insert(KomgaUser("user2@example.org", "p"))

    val users = komgaUserDao.findAll()

    assertThat(users).hasSize(2)
    assertThat(users.map { it.email }).containsExactlyInAnyOrder(
      "user1@example.org",
      "user2@example.org",
    )
  }

  @Test
  fun `given some users when counting then proper count is returned`() {
    komgaUserDao.insert(KomgaUser("user1@example.org", "p"))
    komgaUserDao.insert(KomgaUser("user2@example.org", "p"))

    val count = komgaUserDao.count()

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `given existing user when finding by id then user is returned`() {
    val existing = KomgaUser("user1@example.org", "p")
    komgaUserDao.insert(existing)

    val user = komgaUserDao.findByIdOrNull(existing.id)

    assertThat(user).isNotNull
  }

  @Test
  fun `given non-existent user when finding by id then null is returned`() {
    val user = komgaUserDao.findByIdOrNull("38473")

    assertThat(user).isNull()
  }

  @Test
  fun `given existing user when deleting then user is deleted`() {
    val existing = KomgaUser("user1@example.org", "p")
    komgaUserDao.insert(existing)

    komgaUserDao.delete(existing.id)

    assertThat(komgaUserDao.count()).isEqualTo(0)
  }

  @Test
  fun `given users when checking if exists by email then return true or false`() {
    komgaUserDao.insert(
      KomgaUser("user1@example.org", "p"),
    )

    val exists = komgaUserDao.existsByEmailIgnoreCase("USER1@EXAMPLE.ORG")
    val notExists = komgaUserDao.existsByEmailIgnoreCase("USER2@EXAMPLE.ORG")

    assertThat(exists).isTrue()
    assertThat(notExists).isFalse()
  }

  @Test
  fun `given users when finding by email then return user`() {
    komgaUserDao.insert(
      KomgaUser("user1@example.org", "p"),
    )

    val found = komgaUserDao.findByEmailIgnoreCaseOrNull("USER1@EXAMPLE.ORG")
    val notFound = komgaUserDao.findByEmailIgnoreCaseOrNull("USER2@EXAMPLE.ORG")

    assertThat(found).isNotNull
    assertThat(found?.email).isEqualTo("user1@example.org")
    assertThat(notFound).isNull()
  }
}
