package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotBlank

@Entity
class Serie(
    @NotBlank
    val name: String,

    val url: URL,
    val updated: LocalDateTime
) {
  @Id
  @GeneratedValue
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