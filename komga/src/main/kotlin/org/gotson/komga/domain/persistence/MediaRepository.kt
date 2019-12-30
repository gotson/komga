package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Media
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MediaRepository : JpaRepository<Media, Long>
