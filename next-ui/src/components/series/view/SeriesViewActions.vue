<template>
  <div class="d-flex ga-2">
    <v-btn
      prepend-icon="i-mdi:play"
      :text="
        inProgress
          ? intl.formatMessage({
              description: 'Series view: resume reading button',
              defaultMessage: 'Resume',
              id: '/rV68E',
            })
          : intl.formatMessage({
              description: 'Series view: read button',
              defaultMessage: 'Read',
              id: 'EBsaDz',
            })
      "
      :disabled="!canRead"
      @click="readFirstBook()"
    />

    <v-icon-btn
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Series view: read incognito button: tooltip',
          defaultMessage: 'Private reading session',
          id: 'DLUIbm',
        })
      "
      icon="i-mdi:incognito"
      :disabled="!canRead"
      @click="readFirstBook(true)"
    />

    <v-icon-btn
      v-if="isRead"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Series view: mark unread button: tooltip',
          defaultMessage: 'Mark as unread',
          id: 'RUN9vs',
        })
      "
      icon="i-mdi:check-circle"
      v-bind="getAction(SeriesAction.MARK_UNREAD)"
    />

    <v-icon-btn
      v-if="!isRead"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Series view: mark read button: tooltip',
          defaultMessage: 'Mark as read',
          id: 'nc+4RD',
        })
      "
      icon="i-mdi:check-circle-outline"
      v-bind="getAction(SeriesAction.MARK_READ)"
    />

    <v-icon-btn
      v-if="isAdmin"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Series view: edit button: tooltip',
          defaultMessage: 'Edit',
          id: 'FNryZW',
        })
      "
      icon="i-mdi:pencil"
      v-bind="getAction(SeriesAction.EDIT_METADATA)"
    />
  </div>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useIntl } from 'vue-intl'
import { useCurrentUser } from '@/colada/users'
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { useSeries } from '@/composables/series/useSeries'
import { SeriesAction } from '@/types/series'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'

const intl = useIntl()

const props = defineProps<{
  series: components['schemas']['SeriesDto']
}>()

const { isAdmin } = useCurrentUser()
// const { isRead, inProgress } = useBookReadProgress(() => props.book)
const { getAction } = useSeriesActions(() => props.series)
const { canRead, isRead, inProgress } = useSeries(() => props.series)
const { readFirstBook } = useSeriesBooks(props.series.id)
</script>
