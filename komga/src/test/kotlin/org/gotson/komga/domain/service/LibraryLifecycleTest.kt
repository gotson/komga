package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

@SpringBootTest
class LibraryLifecycleTest(
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
) {
  @AfterEach
  fun `clear repositories`() {
    libraryRepository.deleteAll()
  }

  @Nested
  inner class Add {
    @Test
    fun `when adding library with non-existent root folder then exception is thrown`() {
      // when
      val thrown = catchThrowable { libraryLifecycle.addLibrary(Library("test", URL("file:/non-existent"))) }

      // then
      assertThat(thrown).isInstanceOf(FileNotFoundException::class.java)
    }

    @Test
    fun `when adding library with non-directory root folder then exception is thrown`() {
      // when
      val thrown =
        catchThrowable {
          libraryLifecycle.addLibrary(
            Library(
              "test",
              Files
                .createTempFile(null, null)
                .also { it.toFile().deleteOnExit() }
                .toUri()
                .toURL(),
            ),
          )
        }

      // then
      assertThat(thrown).isInstanceOf(DirectoryNotFoundException::class.java)
    }

    @Test
    fun `given existing library when adding library with same name then exception is thrown`(
      @TempDir path1: Path,
      @TempDir path2: Path,
    ) {
      // given
      libraryLifecycle.addLibrary(Library("test", path1.toUri().toURL()))

      // when
      val thrown =
        catchThrowable {
          libraryLifecycle.addLibrary(Library("test", path2.toUri().toURL()))
        }

      // then
      assertThat(thrown).isInstanceOf(DuplicateNameException::class.java)
    }

    @Test
    fun `given existing library when adding library with root folder as child of existing library then exception is thrown`(
      @TempDir parent: Path,
    ) {
      // given
      libraryLifecycle.addLibrary(Library("parent", parent.toUri().toURL()))

      // when
      val child = Files.createTempDirectory(parent, "")
      val thrown =
        catchThrowable {
          libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(PathContainedInPath::class.java)
        .hasMessageContaining("child")
    }

    @Test
    fun `given existing library when adding library with root folder as parent of existing library then exception is thrown`(
      @TempDir parent: Path,
    ) {
      // given
      val child = Files.createTempDirectory(parent, null)
      libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))

      // when
      val thrown =
        catchThrowable {
          libraryLifecycle.addLibrary(Library("parent", parent.toUri().toURL()))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(PathContainedInPath::class.java)
        .hasMessageContaining("parent")
    }
  }

  @Nested
  inner class Update {
    private lateinit var rootFolder: Path
    private lateinit var library: Library

    @BeforeAll
    fun setup(
      @TempDir root: Path,
    ) {
      rootFolder = root
      library = Library("Existing", rootFolder.toUri().toURL())
    }

    @Test
    fun `given existing library when updating with non-existent root folder then exception is thrown`() {
      // given
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val toUpdate = existing.copy(name = "test", root = URL("file:/non-existent"))
      val thrown = catchThrowable { libraryLifecycle.updateLibrary(toUpdate) }

      // then
      assertThat(thrown).isInstanceOf(FileNotFoundException::class.java)
    }

    @Test
    fun `given existing library when updating with non-directory root folder then exception is thrown`() {
      // given
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val toUpdate =
        existing.copy(
          name = "test",
          root =
            Files
              .createTempFile(null, null)
              .also { it.toFile().deleteOnExit() }
              .toUri()
              .toURL(),
        )
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown).isInstanceOf(DirectoryNotFoundException::class.java)
    }

    @Test
    fun `given single existing library when updating library with same name then it is updated`() {
      // given
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(existing)
        }

      // then
      assertThat(thrown).doesNotThrowAnyException()
    }

    @Test
    fun `given existing library when updating library with same name then exception is thrown`(
      @TempDir path1: Path,
      @TempDir path2: Path,
    ) {
      // given
      libraryLifecycle.addLibrary(Library("test", path1.toUri().toURL()))
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val toUpdate = existing.copy(name = "test", root = path2.toUri().toURL())
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown).isInstanceOf(DuplicateNameException::class.java)
    }

    @Test
    fun `given single existing library when updating library with root folder as child of existing library then no exception is thrown`() {
      // given
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val child = Files.createTempDirectory(rootFolder, "")
      val toUpdate = existing.copy(root = child.toUri().toURL())
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown).doesNotThrowAnyException()
    }

    @Test
    fun `given existing library when updating library with root folder as child of existing library then exception is thrown`(
      @TempDir parent: Path,
    ) {
      // given
      libraryLifecycle.addLibrary(Library("parent", parent.toUri().toURL()))
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val child = Files.createTempDirectory(parent, "")
      val toUpdate = existing.copy(root = child.toUri().toURL())
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown)
        .isInstanceOf(PathContainedInPath::class.java)
        .hasMessageContaining("child")
    }

    @Test
    fun `given single existing library when updating library with root folder as parent of existing library then no exception is thrown`(
      @TempDir parent: Path,
    ) {
      // given
      val child = Files.createTempDirectory(parent, null)
      val existing = libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))

      // when
      val toUpdate = existing.copy(root = parent.toUri().toURL())
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown).doesNotThrowAnyException()
    }

    @Test
    fun `given existing library when updating library with root folder as parent of existing library then exception is thrown`(
      @TempDir parent: Path,
    ) {
      // given
      val child = Files.createTempDirectory(parent, null)
      libraryLifecycle.addLibrary(Library("child", child.toUri().toURL()))
      val existing = libraryLifecycle.addLibrary(library)

      // when
      val toUpdate = existing.copy(root = parent.toUri().toURL())
      val thrown =
        catchThrowable {
          libraryLifecycle.updateLibrary(toUpdate)
        }

      // then
      assertThat(thrown)
        .isInstanceOf(PathContainedInPath::class.java)
        .hasMessageContaining("parent")
    }
  }
}
