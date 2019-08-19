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
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "book_metadata")
class BookMetadata(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: Status = Status.UNKNOWN,

    @Column(name = "media_type")
    val mediaType: String? = null,

    pages: List<BookPage> = emptyList()
) {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  val id: Long = 0

  @OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "metadata")
  lateinit var book: Book

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "book_metadata_pages", joinColumns = [JoinColumn(name = "book_metadata_id")])
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