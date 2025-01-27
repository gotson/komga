package org.gotson.komga.infrastructure.security

/**
 * Service interface for encoding tokens.
 * Contrary to password encoding, token encoding is deterministic, so that lookups can be done using
 * only the token, without a username.
 */
fun interface TokenEncoder {
  fun encode(rawPassword: String): String
}
