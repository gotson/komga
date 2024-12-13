package org.gotson.komga

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.ScanResult
import org.gotson.komga.domain.model.Series

fun Map<Series, List<Book>>.toScanResult() = ScanResult(this, emptyList())
