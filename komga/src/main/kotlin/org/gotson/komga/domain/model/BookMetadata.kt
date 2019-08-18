package org.gotson.komga.domain.model

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class BookMetadata(
    @Enumerated(EnumType.STRING)
    val status: Status = Status.UNKNOWN,
    val mediaType: String? = null,
    pages: List<BookPage> = emptyList()
) {
  @Id
  @GeneratedValue
  val id: Long = 0

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  lateinit var book: Book

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "book_metadata_pages")
  @Column(name = "pages")
  private val _pages: MutableList<BookPage> = mutableListOf()

  val pages: List<BookPage>
    get() = _pages.toList()

  init {
    _pages.addAll(pages)
  }
}

enum class Status {
  UNKNOWN, ERROR, READY, UNSUPPORTED
}