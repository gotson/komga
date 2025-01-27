package org.gotson.komga.infrastructure.transaction

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class TransactionConfiguration {
  @Bean
  fun transactionTemplate(transactionManager: PlatformTransactionManager) = TransactionTemplate(transactionManager)
}
