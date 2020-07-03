import java.net.URL
import java.nio.file.Paths

fun URL.toFilePath(): String =
  Paths.get(this.toURI()).toString()

fun filePathToUrl(filePath: String): URL =
  Paths.get(filePath).toUri().toURL()
