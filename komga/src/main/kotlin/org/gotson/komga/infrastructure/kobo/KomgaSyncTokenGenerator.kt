package org.gotson.komga.infrastructure.kobo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.gotson.komga.domain.model.KomgaSyncToken
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class KomgaSyncTokenGenerator(
  private val objectMapper: ObjectMapper,
) {
  private val base64Encoder by lazy { Base64.getEncoder().withoutPadding() }
  private val base64Decoder by lazy { Base64.getDecoder() }

  fun fromBase64(base64Token: String): KomgaSyncToken {
    if (base64Token.contains('.')) {
      return KomgaSyncToken(rawKoboSyncToken = base64Token)
    }

    return objectMapper.readValue(base64Decoder.decode(base64Token))
  }

  fun toBase64(token: KomgaSyncToken): String {
    return base64Encoder.encodeToString(objectMapper.writeValueAsString(token).toByteArray())
  }
}
