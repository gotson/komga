package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.FileNotFoundException
import java.nio.file.Files

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class LibraryLifecycleTest(
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle
) {

  @AfterEach
  fun `clear repositories`() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `when adding library with non-existent root folder then exception is thrown`() {
    // when
    val thrown = catchThrowable { libraryLifecycle.addLibrary(Library("test", "/non-existent")) }

    // then
    assertThat(thrown).isInstanceOf(FileNotFoundException::class.java)
  }

  @Test
  fun `when adding library with non-directory root folder then exception is thrown`() {
    // when
    val thrown = catchThrowable {
      libraryLifecycle.addLibrary(Library("test", Files.createTempFile(null, null).toUri().toURL()))
    }

    // then
    assertThat(thrown).isInstanceOf(DirectoryNotFoundException::class.java)
  }

  @Test
  fun `given existing library when adding library with same name then exception is thrown`() {
    // given
    libraryLifecycle.addLibrary(Library("test", Files.createTempDirectory(null).toUri().toURL()))

    // when
    val thrown = catchThrowable {
      libraryLifecycle.addLibrary(Library("test", Files.createTempDirectory(null).toUri().toURL()))
    }

    // then
    assertThat(thrown).isInstanceOf(DuplicateNameException::class.java)
  }

  @Test
  fun `given existing library when adding library with root folder as child of existing library then exception is thrown`() {
    // given
    val parent = Files.createTempDirectory(null)
    libraryLifecycle.addLibrary(Library("parent", parent.toUri().toURL()))

    // when
    val child = Files.createTempDirectory(parent, "")
    val thrown = catchThrowable {
      libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))
    }

    // then
    assertThat(thrown)
      .isInstanceOf(PathContainedInPath::class.java)
      .hasMessageContaining("child")
  }

  @Test
  fun `given existing library when adding library with root folder as parent of existing library then exception is thrown`() {
    // given
    val parent = Files.createTempDirectory(null)
    val child = Files.createTempDirectory(parent, null)
    libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))

    // when
    val thrown = catchThrowable {
      libraryLifecycle.addLibrary(Library("parent", parent.toUri().toURL()))
    }

    // then
    assertThat(thrown)
      .isInstanceOf(PathContainedInPath::class.java)
      .hasMessageContaining("parent")
  }

}
