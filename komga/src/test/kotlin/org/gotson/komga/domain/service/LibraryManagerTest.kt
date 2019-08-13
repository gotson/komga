package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class LibraryManagerTest(
    @Autowired private val libraryManager: LibraryManager,
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val bookRepository: BookRepository
) {

  @Test
  fun `given existing Serie when adding files and scanning then only updated Books are persisted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      Files.createFile(root.resolve("file1.cbz"))

      val library = Library("test", "/root", fs)
      libraryManager.scanRootFolder(library)

      // when
      Files.createFile(root.resolve("file2.cbz"))
      libraryManager.scanRootFolder(library)

      // then
      val series = serieRepository.findAll()

      assertThat(series).hasSize(1)
      assertThat(series.first().books).hasSize(2)
      assertThat(series.first().books.map { it.name }).containsExactlyInAnyOrder("file1", "file2")
    }
  }

  @Test
  fun `given existing Serie when deleting all books and scanning then Series and Books are removed`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      Files.createFile(root.resolve("file1.cbz"))

      val library = Library("test", "/root", fs)
      libraryManager.scanRootFolder(library)

      // when
      Files.delete(root.resolve("file1.cbz"))
      libraryManager.scanRootFolder(library)

      // then
      assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(0)
      assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(0)
    }
  }
}