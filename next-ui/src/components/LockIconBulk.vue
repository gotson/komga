<template>
  <KTooltip location="bottom">
    <template #activator="{ props: activatorProps }">
      <v-btn
        v-bind="activatorProps"
        :color="getLockProps(model)?.color"
        :icon="getLockProps(model)?.icon"
        variant="text"
        tabindex="-1"
        @click.stop="cycleState()"
      />
    </template>
    {{ tooltip }}
  </KTooltip>
</template>

<script setup lang="ts">
import type { BulkInputProps, BulkItemSingleField } from '@/types/bulk'
import { initializeBulkEdit } from '@/functions/bulkEdit'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const model = defineModel<BulkItemSingleField<boolean>>()
const props = defineProps<BulkInputProps<boolean>>()

const { initialValue, isMixed } = initializeBulkEdit(props, false)
if (!model.value) {
  model.value = initialValue
}

function cycleState() {
  model.value = computeNextState(model.value)
}

/**
 * Cycle: all -> none -> (mixed) -> all
 * Mixed is skipped if the initial state was not mixed.
 */
function computeNextState(
  current: BulkItemSingleField<boolean> | undefined,
): BulkItemSingleField<boolean> {
  if (current?.state === 'all' && current.value) return { state: 'all', value: false }
  else if (current?.state === 'all' && !current.value && isMixed) return { state: 'mixed' }
  else return { state: 'all', value: true }
}

function getLockProps(item: BulkItemSingleField<boolean> | undefined):
  | {
      color: string
      icon: string
    }
  | undefined {
  if (!item) return undefined
  switch (item.state) {
    case 'mixed':
      return {
        color: 'warning',
        icon: 'i-mdi:lock-question',
      }
    case 'all':
      return item.value
        ? {
            color: 'primary',
            icon: 'i-mdi:lock',
          }
        : {
            color: 'medium-emphasis',
            icon: 'i-mdi:lock-open',
          }
  }
}

// compute tooltip text according to the future state
const tooltip = computed(() => {
  const nextState = computeNextState(model.value)
  switch (nextState.state) {
    case 'mixed':
      return intl.formatMessage({
        description: 'Bulk lock icon: tooltip - mixed',
        defaultMessage: 'Restore initial value',
        id: '0c6DrH',
      })
    case 'all':
      return nextState.value
        ? intl.formatMessage({
            description: 'Bulk lock icon: tooltip - lock',
            defaultMessage: 'Lock field',
            id: 'tcXhCG',
          })
        : intl.formatMessage({
            description: 'Bulk lock icon: tooltip - unlock',
            defaultMessage: 'Unlock field',
            id: 'qq92EF',
          })
  }
})
</script>
