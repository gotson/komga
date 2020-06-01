package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class FileSystemScannerTest {

  private val komgaProperties = KomgaProperties().apply {
    librariesScanDirectoryExclusions = listOf("#recycle")
  }

  private val scanner = FileSystemScanner(komgaProperties)

  @Test
  fun `given empty root directory when scanning then return empty list`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      // when
      val scan = scanner.scanRootFolder(root)

      // then
      assertThat(scan).isEmpty()
    }
  }

  @Test
  fun `given root directory with only files when scanning then return 1 series containing those files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val files = listOf("file1.cbz", "file2.cbz")
      files.forEach { Files.createFile(root.resolve(it)) }

      // when
      val scan = scanner.scanRootFolder(root)
      val series = scan.keys.first()
      val books = scan.getValue(series)

      // then
      assertThat(scan).hasSize(1)
      assertThat(books).hasSize(2)
      assertThat(books.map { it.name }).containsExactlyInAnyOrderElementsOf(files.map { FilenameUtils.removeExtension(it) })
    }
  }

  @Test
  fun `given directory with unsupported files when scanning then return a series excluding those files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val files = listOf("file1.cbz", "file2.txt", "file3")
      files.forEach { Files.createFile(root.resolve(it)) }

      // when
      val scan = scanner.scanRootFolder(root)
      val series = scan.keys.first()
      val books = scan.getValue(series)

      // then
      assertThat(scan).hasSize(1)
      assertThat(books).hasSize(1)
      assertThat(books.map { it.name }).containsExactly("file1")
    }
  }

  @Test
  fun `given directory with sub-directories containing files when scanning then return 1 series per folder containing direct files as books`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val subDirs = listOf(
        "series1" to listOf("volume1.cbz", "volume2.cbz"),
        "series2" to listOf("book1.cbz", "book2.cbz")
      ).toMap()

      subDirs.forEach { (dir, files) ->
        makeSubDir(root, dir, files)
      }

      // when
      val scan = scanner.scanRootFolder(root)
      val series = scan.keys

      // then
      assertThat(scan).hasSize(2)

      assertThat(series.map { it.name }).containsExactlyInAnyOrderElementsOf(subDirs.keys)
      series.forEach { s ->
        assertThat(scan.getValue(s).map { it.name }).containsExactlyInAnyOrderElementsOf(subDirs[s.name]?.map { FilenameUtils.removeExtension(it) })
      }
    }
  }

  @Test
  fun `given directory structure with excluded directories when scanning then excluded directories are not returned`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      val dir1 = makeSubDir(root, "dir1", listOf("comic.cbz"))
      makeSubDir(dir1, "subdir1", listOf("comic2.cbz"))
      val recycle = makeSubDir(root, "#recycle", listOf("trash.cbz"))
      makeSubDir(recycle, "subtrash", listOf("trash2.cbz"))

      // when
      val scan = scanner.scanRootFolder(root)

      // then
      assertThat(scan).hasSize(2)

      assertThat(scan.keys.map { it.name }).containsExactlyInAnyOrder("dir1", "subdir1")
      assertThat(scan.values.flatMap { list -> list.map { it.name } }).containsExactlyInAnyOrder("comic", "comic2")
    }
  }

  @Test
  fun `given file with mixed-case extension when scanning then files are returned`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)

      makeSubDir(root, "dir1", listOf("comic.Cbz", "comic2.CBR"))

      // when
      val scan = scanner.scanRootFolder(root)

      // then
      assertThat(scan).hasSize(1)

      assertThat(scan.keys.map { it.name }).containsExactlyInAnyOrder("dir1")
      assertThat(scan.values.flatMap { list -> list.map { it.name } }).containsExactlyInAnyOrder("comic", "comic2")
    }
  }

  private fun makeSubDir(root: Path, name: String, files: List<String>): Path {
    val dir = root.resolve(name)
    Files.createDirectory(dir)
    files.forEach { Files.createFile(root.resolve(root.fileSystem.getPath(name, it))) }
    return dir
  }
}
