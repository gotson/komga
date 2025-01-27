package org.gotson.komga.domain.model

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface MediaExtension

class ProxyExtension private constructor(
  val extensionClassName: String,
) : MediaExtension {
  companion object {
    fun of(extensionClass: String?): ProxyExtension? =
      extensionClass?.let {
        val kClass = Class.forName(extensionClass).kotlin
        if (kClass.qualifiedName != MediaExtension::class.qualifiedName && kClass.isSubclassOf(MediaExtension::class))
          ProxyExtension(extensionClass)
        else
          null
      }
  }

  inline fun <reified T> proxyForType(): Boolean = T::class.qualifiedName == extensionClassName

  fun proxyForType(clazz: KClass<out Any>): Boolean = clazz.qualifiedName == extensionClassName
}

data class MediaExtensionEpub(
  val toc: List<EpubTocEntry> = emptyList(),
  val landmarks: List<EpubTocEntry> = emptyList(),
  val pageList: List<EpubTocEntry> = emptyList(),
  val isFixedLayout: Boolean = false,
  val positions: List<R2Locator> = emptyList(),
) : MediaExtension
