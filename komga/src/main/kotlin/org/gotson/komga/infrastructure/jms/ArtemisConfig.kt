package org.gotson.komga.infrastructure.jms

import org.apache.activemq.artemis.api.core.RoutingType
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration
import org.apache.activemq.artemis.core.settings.impl.AddressSettings
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer
import org.springframework.context.annotation.Configuration
import org.apache.activemq.artemis.core.config.Configuration as ArtemisConfiguration

const val QUEUE_UNIQUE_ID = "unique_id"
const val QUEUE_TYPE = "type"
const val QUEUE_TASKS = "tasks.background"
const val QUEUE_TASKS_TYPE = "task"
const val QUEUE_TASKS_SELECTOR = "$QUEUE_TYPE = '$QUEUE_TASKS_TYPE'"

@Configuration
class ArtemisConfig : ArtemisConfigurationCustomizer {
  override fun customize(configuration: ArtemisConfiguration?) {
    configuration?.let {
      // default is 90, meaning the queue would block if disk is 90% full. Set it to 100 to avoid blocking.
      it.maxDiskUsage = 100
      // disable prefetch, ensures messages stay in the queue and last value can have desired effect
      it.addAddressesSetting(QUEUE_TASKS, AddressSettings().apply {
        defaultConsumerWindowSize = 0
      })
      it.addQueueConfiguration(
        CoreQueueConfiguration()
          .setAddress(QUEUE_TASKS)
          .setName(QUEUE_TASKS)
          .setLastValueKey(QUEUE_UNIQUE_ID)
          .setRoutingType(RoutingType.ANYCAST)
      )
    }
  }
}
