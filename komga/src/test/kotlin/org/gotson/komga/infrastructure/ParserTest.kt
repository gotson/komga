package org.gotson.komga.infrastructure

import mu.KotlinLogging
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.junit.jupiter.api.Test
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class ParserTest {

  @Test
  fun parseTest() {
    val filePath = """D:\files\comics\Chrononauts\Chrononauts 001 (2015) (Digital) (Zone-Empire).cbz"""
    val path = Paths.get(filePath)

    val tika = TikaConfig()

    val metadata = Metadata().also {
      it[Metadata.RESOURCE_NAME_KEY] = path.fileName.toString()
    }
    val mimeType = tika.detector.detect(TikaInputStream.get(path), metadata)
    logger.info { mimeType }
    logger.info { tika.detector.detect(TikaInputStream.get(path), Metadata()) }
  }
}