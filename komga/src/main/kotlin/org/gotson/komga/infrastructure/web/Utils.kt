package org.gotson.komga.infrastructure.web

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun URL.toFilePath(): String =
  Paths.get(this.toURI()).toString()

fun filePathToUrl(filePath: String): URL =
  Paths.get(filePath).toUri().toURL()

fun ResponseEntity.BodyBuilder.setCachePrivate() =
  this.cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS)
    .cachePrivate()
    .mustRevalidate()
  )
