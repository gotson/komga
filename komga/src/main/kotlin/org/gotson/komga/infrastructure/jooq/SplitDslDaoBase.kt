package org.gotson.komga.infrastructure.jooq

import org.jooq.DSLContext
import org.springframework.transaction.support.TransactionSynchronizationManager

abstract class SplitDslDaoBase {
  val dslRW: DSLContext
  private val _dslRO: DSLContext

  constructor(dslRW: DSLContext, dslRO: DSLContext) {
    this.dslRW = dslRW
    this._dslRO = dslRO
  }

  val dslRO: DSLContext
    get() =
      if (TransactionSynchronizationManager.isActualTransactionActive() && !TransactionSynchronizationManager.isCurrentTransactionReadOnly())
        dslRW
      else
        _dslRO
}
