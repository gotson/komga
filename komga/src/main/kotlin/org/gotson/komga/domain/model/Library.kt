package org.gotson.komga.domain.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "library")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "cache.library")
class Library(
  @NotBlank
  @Column(name = "name", nullable = false, unique = true)
  val name: String,

  @NotBlank
  @Column(name = "root", nullable = false)
  val root: URL
) : AuditableEntity() {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  var id: Long = 0

  constructor(name: String, root: String) : this(name, Paths.get(root).toUri().toURL())

  fun path(): Path = Paths.get(this.root.toURI())
}
