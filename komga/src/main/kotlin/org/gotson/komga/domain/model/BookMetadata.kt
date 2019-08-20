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
import javax.persistence.Lob
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "book_metadata")
class BookMetadata(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: Status = Status.UNKNOWN,

    @Column(name = "media_type")
    var mediaType: String? = null,

    @Column(name = "thumbnail")
    @Lob
    var thumbnail: ByteArray? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_metadata_page", joinColumns = [JoinColumn(name = "book_metadata_id")])
    var pages: MutableList<BookPage> = mutableListOf()
) {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  val id: Long = 0

  @OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "metadata")
  lateinit var book: Book

  fun reset() {
    status = Status.UNKNOWN
    mediaType = null
    thumbnail = null
    pages = mutableListOf()
  }
}

enum class Status {
  UNKNOWN, ERROR, READY, UNSUPPORTED
}