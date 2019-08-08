package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Serie
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SerieRepository : JpaRepository<Serie, Long>