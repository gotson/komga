package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LibraryRepository : JpaRepository<Library, Long> {
  fun existsByName(name: String): Boolean
}
