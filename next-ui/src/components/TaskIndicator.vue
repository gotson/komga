<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-tooltip:bottom="
          $formatMessage({
            description: 'Task Indicator button: tooltip',
            defaultMessage: 'Activity',
            id: 'n0yuoo',
          })
        "
        v-bind="props"
        icon
        variant="flat"
        color=""
        :aria-label="
          $formatMessage({
            description: 'Task Indicator button: aria-label',
            defaultMessage: 'activity details',
            id: 'CMuDjt',
          })
        "
      >
        <v-icon icon="i-tabler:activity"></v-icon>
        <v-progress-circular
          v-if="taskStore.count > 0"
          indeterminate
          color="secondary"
          size="48"
          width="2"
          class="position-absolute"
        />
      </v-btn>
    </template>

    <v-card>
      <template #text>
        <SimpleDataTable
          v-if="taskStore.count > 0"
          :rows="rows"
          :uppercase-headers="false"
        />
        <div v-else>
          {{
            $formatMessage({
              description: 'Task Indicator menu: no activity',
              defaultMessage: 'No activity',
              id: 'dLE2PW',
            })
          }}
        </div>
      </template>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { useTaskQueueStore } from '@/stores/task-queue'
import { useIntl } from 'vue-intl'
import { taskNameMessage } from '@/utils/i18n/enum/task'

const intl = useIntl()
const taskStore = useTaskQueueStore()

const rows = computed(() =>
  Object.entries(taskStore.countByType).map(([task, count]) => ({
    header: task in taskNameMessage ? intl.formatMessage(taskNameMessage[task]!) : task,
    data: count + '',
  })),
)
</script>

<script lang="ts"></script>

<style scoped></style>
