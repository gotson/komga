package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class AlternateTitleDeserializer : JsonDeserializer<List<AlternateTitleEntry>>() {
  override fun deserialize(
    p: JsonParser,
    ctxt: DeserializationContext,
  ): List<AlternateTitleEntry> {
    val node: JsonNode = p.codec.readTree(p)
    val result = mutableListOf<AlternateTitleEntry>()

    if (node.isArray) {
      for (element in node) {
        when {
          // New format: {"title": "...", "language": "..."}
          element.isObject && element.has("title") -> {
            result.add(
              AlternateTitleEntry(
                title = element.get("title").asText(),
                language = element.get("language")?.asText(),
              ),
            )
          }
          // Old format: just string
          element.isTextual -> {
            result.add(
              AlternateTitleEntry(
                title = element.asText(),
                language = null,
              ),
            )
          }
        }
      }
    }

    return result
  }
}
