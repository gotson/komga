package org.gotson.komga.infrastructure.language

import java.util.Enumeration
import java.util.SortedMap

fun <T> List<T>.toIndexedMap(): SortedMap<Int, T> =
  mapIndexed { i, e -> i to e }.toMap().toSortedMap()

fun <T> List<T>.toEnumeration(): Enumeration<T> {
  return object : Enumeration<T> {
    var count = 0

    override fun hasMoreElements(): Boolean {
      return this.count < size
    }

    override fun nextElement(): T {
      if (this.count < size) {
        return get(this.count++)
      }
      throw NoSuchElementException("List enumeration asked for more elements than present")
    }
  }
}
