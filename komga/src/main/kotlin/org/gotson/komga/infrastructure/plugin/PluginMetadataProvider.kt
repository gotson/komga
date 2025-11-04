package org.gotson.komga.infrastructure.plugin

import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.MetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider

/**
 * Plugin interface for book metadata providers.
 * Plugins implementing this interface can extract metadata from books.
 */
interface PluginBookMetadataProvider : KomgaPlugin {
  /**
   * Get book metadata from a book.
   * @param book The book with media information
   * @return BookMetadataPatch containing the extracted metadata, or null if extraction failed
   */
  fun getBookMetadata(book: BookWithMedia): BookMetadataPatch?

  /**
   * Get the capabilities of this provider.
   * Indicates which metadata fields this provider can extract.
   */
  fun getCapabilities(): Set<BookMetadataPatchCapability>
}

/**
 * Plugin interface for series metadata providers.
 * Plugins implementing this interface can extract series-level metadata.
 */
interface PluginSeriesMetadataProvider : KomgaPlugin {
  /**
   * Get series metadata.
   * @param series The series to extract metadata for
   * @return SeriesMetadataPatch containing the extracted metadata, or null if extraction failed
   */
  fun getSeriesMetadata(series: Series): SeriesMetadataPatch?
}

/**
 * Plugin interface for series metadata providers that derive series metadata from books.
 */
interface PluginSeriesMetadataFromBookProvider : KomgaPlugin {
  /**
   * Get series metadata from a book.
   * @param book The book to derive series metadata from
   * @param appendVolumeToTitle Whether to append volume information to the title
   * @return SeriesMetadataPatch containing the derived metadata, or null if extraction failed
   */
  fun getSeriesMetadataFromBook(book: BookWithMedia, appendVolumeToTitle: Boolean): SeriesMetadataPatch?

  /**
   * Whether this provider supports appending volume to title.
   */
  fun supportsAppendVolume(): Boolean = false
}

/**
 * Adapter to bridge plugin providers to Komga's internal provider interfaces.
 */
internal class PluginBookMetadataProviderAdapter(
  private val plugin: PluginBookMetadataProvider,
  private val pluginId: String,
) : BookMetadataProvider {
  override val capabilities: Set<BookMetadataPatchCapability>
    get() = plugin.getCapabilities()

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? =
    try {
      plugin.getBookMetadata(book)
    } catch (e: Exception) {
      null
    }

  override fun shouldLibraryHandlePatch(library: Library, target: MetadataPatchTarget): Boolean =
    library.pluginConfigurations[pluginId]?.enabled ?: false
}

internal class PluginSeriesMetadataProviderAdapter(
  private val plugin: PluginSeriesMetadataProvider,
  private val pluginId: String,
) : SeriesMetadataProvider {
  override fun getSeriesMetadata(series: Series): SeriesMetadataPatch? =
    try {
      plugin.getSeriesMetadata(series)
    } catch (e: Exception) {
      null
    }

  override fun shouldLibraryHandlePatch(library: Library, target: MetadataPatchTarget): Boolean =
    library.pluginConfigurations[pluginId]?.enabled ?: false
}

internal class PluginSeriesMetadataFromBookProviderAdapter(
  private val plugin: PluginSeriesMetadataFromBookProvider,
  private val pluginId: String,
) : SeriesMetadataFromBookProvider {
  override val supportsAppendVolume: Boolean
    get() = plugin.supportsAppendVolume()

  override fun getSeriesMetadataFromBook(book: BookWithMedia, appendVolumeToTitle: Boolean): SeriesMetadataPatch? =
    try {
      plugin.getSeriesMetadataFromBook(book, appendVolumeToTitle)
    } catch (e: Exception) {
      null
    }

  override fun shouldLibraryHandlePatch(library: Library, target: MetadataPatchTarget): Boolean =
    library.pluginConfigurations[pluginId]?.enabled ?: false
}
