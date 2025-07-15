package org.gotson.komga.infrastructure.mediacontainer.divina

import com.github.gotson.nightcompress.Archive
import com.github.gotson.nightcompress.ReadSupportCompression
import com.github.gotson.nightcompress.ReadSupportFilter
import com.github.gotson.nightcompress.ReadSupportFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Configuration(proxyBeanMethods = false)
class Rar5Configuration : BeanDefinitionRegistryPostProcessor {
  override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {}

  override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    try {
      if (Archive.isAvailable()) {
        val builder = BeanDefinitionBuilder.genericBeanDefinition(Rar5Extractor::class.java).setLazyInit(true)
        registry.registerBeanDefinition("rar5Extractor", builder.beanDefinition)
        logger.info { "Rar5 extractor is enabled" }
      }
    } catch (e: Throwable) {
      logger.warn(e) { "Rar5 extractor count not be loaded" }
    }
  }
}

class Rar5Extractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) : DivinaExtractor {
  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf(MediaType.RAR_5.type)

  override fun getEntries(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry> =
    Archive(path, setOf(ReadSupportCompression.NONE), setOf(ReadSupportFilter.NONE), setOf(ReadSupportFormat.RAR5)).use { rar ->
      generateSequence { rar.nextEntry }
        .map { entry ->
          try {
            val buffer = rar.inputStream.use { it.readBytes() }
            val mediaType = buffer.inputStream().use { contentDetector.detectMediaType(it) }
            val dimension =
              if (analyzeDimensions && contentDetector.isImage(mediaType))
                buffer.inputStream().use { imageAnalyzer.getDimension(it) }
              else
                null
            val fileSize = entry.size
            MediaContainerEntry(name = entry.name, mediaType = mediaType, dimension = dimension, fileSize = fileSize)
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${entry.name}" }
            MediaContainerEntry(name = entry.name, comment = e.message)
          }
        }.sortedWith(compareBy(natSortComparator) { it.name })
        .toList()
    }

  override fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray = Archive.getInputStream(path, setOf(ReadSupportCompression.NONE), setOf(ReadSupportFilter.NONE), setOf(ReadSupportFormat.RAR5), entryName).use { it?.readBytes() ?: ByteArray(0) }
}
