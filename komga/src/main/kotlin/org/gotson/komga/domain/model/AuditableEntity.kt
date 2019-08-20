package org.gotson.komga.domain.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableEntity {
  @CreatedDate
  @Column(name = "created_date", updatable = false, nullable = false)
  var createdDate: LocalDateTime? = null

  @LastModifiedDate
  @Column(name = "last_modified_date", nullable = false)
  var lastModifiedDate: LocalDateTime? = null
}