package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime

fun makeBook(name: String, url: String = "file:/$name") =
    Book(name = name, url = URL(url), updated = LocalDateTime.now())

fun makeSerie(name: String, url: String = "file:/$name", books: List<Book> = listOf()) =
    Serie(name = name, url = URL(url), updated = LocalDateTime.now()).also { it.setBooks(books) }