package org.gotson.komga.infrastructure.language

import java.util.SortedMap

fun <T> List<T>.toIndexedMap(): SortedMap<Int, T> =
  mapIndexed { i, e -> i to e }.toMap().toSortedMap()
