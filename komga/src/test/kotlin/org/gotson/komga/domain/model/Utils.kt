package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.time.LocalDateTime

fun makeBook(name: String, fileLastModified: LocalDateTime = LocalDateTime.now(), libraryId: String = "", seriesId: Long = 0): Book {
  Thread.sleep(5)
  return Book(
    name = name,
    url = URL("file:/$name"),
    fileLastModified = fileLastModified,
    libraryId = libraryId,
    seriesId = seriesId
  )
}

fun makeSeries(name: String, libraryId: String = ""): Series {
  Thread.sleep(5)
  return Series(
    name = name,
    url = URL("file:/$name"),
    fileLastModified = LocalDateTime.now(),
    libraryId = libraryId
  )
}

fun makeLibrary(name: String = "default", url: String = "file:/$name", id: String = TsidCreator.getTsidString()): Library {
  return Library(name, URL(url), id = id)
}

fun makeBookPage(name: String) =
  BookPage(name, "image/png")
