package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class FileSystemScannerTest {

  private val scanner = FileSystemScanner()

  @Test
  fun `given empty root directory when scanning then return empty list`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val series = scanner.scanRootFolder(root)

      assertThat(series).isEmpty()
    }
  }

  @Test
  fun `given root directory with only files when scanning then return 1 serie containing those files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val files = listOf("file1.cbz", "file2.cbz")
      files.forEach { Files.createFile(root.resolve(it)) }

      val series = scanner.scanRootFolder(root)

      assertThat(series).hasSize(1)
      assertThat(series.first().books).hasSize(2)
      assertThat(series.first().books.map { it.name }).containsExactlyInAnyOrderElementsOf(files.map { FilenameUtils.removeExtension(it) })
    }
  }

  @Test
  fun `given directory with unsupported files when scanning then return a serie excluding those files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val files = listOf("file1.cbz", "file2.txt", "file3")
      files.forEach { Files.createFile(root.resolve(it)) }

      val series = scanner.scanRootFolder(root)

      assertThat(series).hasSize(1)
      assertThat(series.first().books).hasSize(1)
      assertThat(series.first().books.map { it.name }).containsExactly("file1")
    }
  }

  @Test
  fun `given directory with sub-directories containing files when scanning then return 1 serie per folder containing direct files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val subDirs = listOf(
          "serie1" to listOf("volume1.cbz", "volume2.cbz"),
          "serie2" to listOf("book1.cbz", "book2.cbz")
      ).toMap()

      subDirs.forEach { (dir, files) ->
        Files.createDirectory(root.resolve(dir))
        files.forEach { Files.createFile(root.resolve(fs.getPath(dir, it))) }
      }

      val series = scanner.scanRootFolder(root)

      assertThat(series).hasSize(2)

      assertThat(series.map { it.name }).containsExactlyInAnyOrderElementsOf(subDirs.keys)
      series.forEach { serie ->
        assertThat(serie.books.map { it.name }).containsExactlyInAnyOrderElementsOf(subDirs[serie.name]?.map { FilenameUtils.removeExtension(it) })
      }
    }
  }
}