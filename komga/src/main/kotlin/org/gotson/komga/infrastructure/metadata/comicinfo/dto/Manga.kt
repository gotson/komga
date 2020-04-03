package org.gotson.komga.infrastructure.metadata.comicinfo.dto

import com.fasterxml.jackson.annotation.JsonCreator
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

enum class Manga(private val value: String) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes"),
  YES_AND_RIGHT_TO_LEFT("YesAndRightToLeft");

  companion object {
    private val map = values().associateBy(Manga::value)
    @JvmStatic
    @JsonCreator
    fun fromValue(value: String) = map[value]
  }
}
