package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookMetadataRepository : JpaRepository<BookMetadata, Long>