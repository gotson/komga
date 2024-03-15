package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.Media
import org.gotson.komga.infrastructure.web.setCachePrivate
import org.springframework.http.ResponseEntity
import java.time.ZoneOffset

fun getBookLastModified(media: Media) =
  media.lastModifiedDate.toInstant(ZoneOffset.UTC).toEpochMilli()

fun ResponseEntity.BodyBuilder.setNotModified(media: Media): ResponseEntity.BodyBuilder =
  this.setCachePrivate().lastModified(getBookLastModified(media))
