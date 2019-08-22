package org.gotson.komga.domain.model

import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.validation.constraints.NotBlank

private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

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

    books: Iterable<Book>

) : AuditableEntity() {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  var id: Long = 0

  @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "serie")
  @OrderColumn(name = "index")
  private var _books: MutableList<Book> = mutableListOf()

  var books: List<Book>
    get() = _books.toList()
    set(value) {
      _books.clear()
      value.forEach { it.serie = this }
      _books.addAll(value.sortedWith(compareBy(natSortComparator) { it.name }))
    }

  init {
    this.books = books.toList()
  }
}