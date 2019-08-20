package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "serie")
class Serie(
    @NotBlank
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "url", nullable = false)
    var url: URL,

    @Column(name = "file_last_modified", nullable = false)
    var fileLastModified: LocalDateTime,

    books: MutableList<Book>

) : AuditableEntity() {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  var id: Long = 0

  @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinTable(name = "serie_books_mapping", joinColumns = [JoinColumn(name = "serie_id")], inverseJoinColumns = [JoinColumn(name = "book_id")])
  var books: MutableList<Book> = mutableListOf()
    set(value) {
      books.clear()
      books.addAll(value)
    }

  init {
    this.books = books
  }
}