<template>
  <div
    style="display: grid; grid-template-columns: max-content 1fr"
    class="align-baseline"
  >
    <template
      v-for="(row, i) in rows"
      :key="i"
    >
      <div class="pa-1 text-body-small text-uppercase first-col">
        {{ row.header }}
      </div>
      <div class="pa-1">
        <div
          v-if="typeof row.data === 'string'"
          v-bind="row.dataProps"
          class="text-body-small mb-1"
        >
          {{ row.data }}
        </div>
        <WrapClamp
          v-else
          :items="row.data"
          :max-lines="2"
        >
          <template #item="{ item }">
            <v-chip
              :to="item.to"
              variant="tonal"
              size="small"
              rounded
              color="primary"
              class="me-1 mb-1"
              :append-icon="item.href ? 'i-mdi:open-in-new' : undefined"
              :href="item.href"
              :rel="item.href ? 'noreferrer' : undefined"
              :target="item.href ? '_blank' : undefined"
              >{{ item.text }}
            </v-chip>
          </template>

          <template #after="{ clamped, toggle, expanded, hiddenItems }">
            <v-btn
              v-if="clamped || expanded"
              variant="text"
              class="ms-1"
              density="compact"
              :append-icon="expanded ? 'i-mdi:chevron-up' : 'i-mdi:chevron-down'"
              :text="
                expanded
                  ? $formatMessage(commonMessages.showLess)
                  : $formatMessage(
                      {
                        description: 'Simple table: show more elements with count',
                        defaultMessage: '+ {count} more',
                        id: 't5NC5O',
                      },
                      { count: hiddenItems.length },
                    )
              "
              @click="toggle"
            />
          </template>
        </WrapClamp>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { WrapClamp } from 'vue-clamp'
import { commonMessages } from '@/utils/i18n/common-messages'

defineProps<{
  rows: TableRow[]
}>()
</script>

<script lang="ts">
import type { RouteLocation } from 'vue-router'

export type TableRow = {
  header: string
  data: string | TableValue[]
  dataProps?: object
}

export type TableValue = {
  text: string
  to?: RouteLocation
  href?: string
}
</script>

<style scoped>
.first-col {
  padding-right: clamp(4px, 4vw, 50px);
}
</style>
