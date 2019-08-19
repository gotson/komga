package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "serie")
class Serie(
    @NotBlank
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "url", nullable = false)
    val url: URL,

    @Column(name = "updated", nullable = false)
    val updated: LocalDateTime
) {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  var id: Long = 0

  @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "serie", orphanRemoval = true)
  private var _books: MutableList<Book> = mutableListOf()
    set(value) {
      value.forEach { it.serie = this }
      field = value
    }

  val books: List<Book>
    get() = _books.toList()

  fun setBooks(books: List<Book>) {
    _books = books.toMutableList()
  }
}