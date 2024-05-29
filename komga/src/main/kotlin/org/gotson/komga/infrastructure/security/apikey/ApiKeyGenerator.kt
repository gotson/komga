package org.gotson.komga.infrastructure.security.apikey

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * API key generator.
 * Uses a random UUID v4 without dashes
 */
@Component
class ApiKeyGenerator {
  fun generate() =
    UUID.randomUUID().toString().replace("-", "")
}
