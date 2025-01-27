package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.time.LocalDateTime

fun makeBook(
  name: String,
  fileLastModified: LocalDateTime = LocalDateTime.now(),
  libraryId: String = "",
  seriesId: String = "",
  url: URL? = null,
  id: String = TsidCreator.getTsid256().toString(),
): Book {
  Thread.sleep(5)
  return Book(
    name = name,
    url = url ?: URL("file:/${name.replace(" ", "_")}"),
    fileLastModified = fileLastModified,
    libraryId = libraryId,
    seriesId = seriesId,
    id = id,
  )
}

fun makeSeries(
  name: String,
  libraryId: String = "",
  url: URL? = null,
): Series {
  Thread.sleep(5)
  return Series(
    name = name,
    url = url ?: URL("file:/${name.replace(" ", "_")}"),
    fileLastModified = LocalDateTime.now(),
    libraryId = libraryId,
  )
}

fun makeLibrary(
  name: String = "default",
  path: String = "file:/${name.replace(" ", "_")}",
  id: String = TsidCreator.getTsid256().toString(),
  url: URL? = null,
): Library =
  Library(
    name = name,
    root = url ?: URL(path),
    id = id,
  )

fun makeBookPage(name: String) = BookPage(name, "image/png")
