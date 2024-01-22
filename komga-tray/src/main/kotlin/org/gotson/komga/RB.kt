package org.gotson.komga

import org.slf4j.helpers.MessageFormatter
import java.util.ResourceBundle

class RB private constructor() {
  companion object {
    private val BUNDLE: ResourceBundle = ResourceBundle.getBundle("org.gotson.komga.messages")

    fun getString(
      key: String,
      vararg args: Any?,
    ): String =
      if (args.isEmpty())
        BUNDLE.getString(key)
      else
        MessageFormatter.arrayFormat(BUNDLE.getString(key), args).message
  }
}
