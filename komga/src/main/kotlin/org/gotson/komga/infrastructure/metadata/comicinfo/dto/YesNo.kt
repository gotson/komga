package org.gotson.komga.infrastructure.metadata.comicinfo.dto

import com.fasterxml.jackson.annotation.JsonCreator
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

enum class YesNo(val value: String) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes");

  companion object {
    private val map = values().associateBy(YesNo::value)
    @JvmStatic
    @JsonCreator
    fun fromValue(value: String) = map[value]
  }
}
