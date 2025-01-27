package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProxyExtensionTest {
  @Test
  fun `when creating proxy of MediaExtension class then it is created`() {
    val proxy = ProxyExtension.of(MediaExtensionEpub::class.qualifiedName)

    assertThat(proxy).isNotNull
  }

  @Test
  fun `when creating proxy of MediaExtension interface then it is null`() {
    val proxy = ProxyExtension.of(MediaExtension::class.qualifiedName)

    assertThat(proxy).isNull()
  }

  @Test
  fun `given proxy extension of class when checking against the class type then it returns true`() {
    val proxy = ProxyExtension.of(MediaExtensionEpub::class.qualifiedName)!!

    assertThat(proxy.proxyForType<MediaExtensionEpub>()).isTrue()
    assertThat(proxy.proxyForType<MediaExtension>()).isFalse()
    assertThat(proxy.proxyForType<Media>()).isFalse()

    assertThat(proxy.proxyForType(MediaExtensionEpub::class)).isTrue()
    assertThat(proxy.proxyForType(MediaExtension::class)).isFalse()
    assertThat(proxy.proxyForType(Media::class)).isFalse()
  }
}
