package org.gotson.komga.application.query

import org.springframework.data.domain.Pageable

fun Pageable.isEmpty() = this.isPaged && this.pageSize == 0
