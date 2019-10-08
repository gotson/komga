package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime

fun makeBook(name: String, url: String = "file:/$name", fileLastModified: LocalDateTime = LocalDateTime.now()): Book {
  Thread.sleep(5)
  return Book(name = name, url = URL(url), fileLastModified = fileLastModified)
}

fun makeSeries(name: String, url: String = "file:/$name", books: List<Book> = listOf()): Series {
  Thread.sleep(5)
  return Series(name = name, url = URL(url), fileLastModified = LocalDateTime.now(), books = books.toMutableList())
}

fun makeLibrary(name: String = "default", url: String = "file:/$name"): Library {
  return Library(name, URL(url))
}

fun makeBookPage(name: String) =
    BookPage(name, "image/png")
