package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.time.LocalDateTime

fun makeBook(name: String, fileLastModified: LocalDateTime = LocalDateTime.now(), libraryId: String = "", seriesId: String = "", url: URL? = null): Book {
  Thread.sleep(5)
  return Book(
    name = name,
    url = url ?: URL("file:/$name"),
    fileLastModified = fileLastModified,
    libraryId = libraryId,
    seriesId = seriesId
  )
}

fun makeSeries(name: String, libraryId: String = "", url: URL? = null): Series {
  Thread.sleep(5)
  return Series(
    name = name,
    url = url ?: URL("file:/$name"),
    fileLastModified = LocalDateTime.now(),
    libraryId = libraryId
  )
}

fun makeLibrary(name: String = "default", url: String = "file:/$name", id: String = TsidCreator.getTsid256().toString()): Library {
  return Library(name, URL(url), id = id)
}

fun makeBookPage(name: String) =
  BookPage(name, "image/png")
