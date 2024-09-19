package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import kotlin.properties.Delegates

class SettingsUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()

  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

  var deleteEmptyCollections: Boolean? = null

  var deleteEmptyReadLists: Boolean? = null

  @get:Positive
  var rememberMeDurationDays: Long? = null

  var renewRememberMeKey: Boolean? = null

  var thumbnailSize: ThumbnailSizeDto? = null

  @get:Positive
  var taskPoolSize: Int? = null

  @get:Positive
  @get:Max(65535)
  var serverPort: Int?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  @get:Pattern(regexp = "^\\/[\\w-\\/]*[a-zA-Z0-9]\$")
  var serverContextPath: String?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var koboProxy: Boolean? = null

  @get:Positive
  @get:Max(65535)
  var koboPort: Int?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var kepubifyPath: String?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }
}
