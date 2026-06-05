<template>
  <div class="d-flex ga-2">
    <v-btn
      prepend-icon="i-mdi:play"
      :text="
        inProgress
          ? intl.formatMessage({
              description: 'Book view: resume reading button',
              defaultMessage: 'Resume',
              id: 'uVMm+G',
            })
          : intl.formatMessage({
              description: 'Book view: read button',
              defaultMessage: 'Read',
              id: 'Cy2QFR',
            })
      "
      :href="bookReaderUrl(book.id, isEpubReader)"
      target="_blank"
      :disabled="!canRead"
    />

    <v-icon-btn
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Book view: read incognito button: tooltip',
          defaultMessage: 'Private reading session',
          id: 'HKQLfY',
        })
      "
      icon="i-mdi:incognito"
      :href="bookReaderUrl(book.id, isEpubReader, true)"
      target="_blank"
      tag="a"
      :disabled="!canRead"
    />

    <v-icon-btn
      v-if="isRead"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Book view: mark unread button: tooltip',
          defaultMessage: 'Mark as unread',
          id: '8IWnN0',
        })
      "
      icon="i-mdi:check-circle"
      v-bind="getAction(BookAction.MARK_UNREAD)"
    />

    <v-icon-btn
      v-if="!isRead"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Book view: mark read button: tooltip',
          defaultMessage: 'Mark as read',
          id: '4G8yIh',
        })
      "
      icon="i-mdi:check-circle-outline"
      v-bind="getAction(BookAction.MARK_READ)"
    />

    <v-icon-btn
      v-if="isAdmin"
      v-tooltip:bottom="
        intl.formatMessage({
          description: 'Book view: edit button: tooltip',
          defaultMessage: 'Edit',
          id: '+rrGSa',
        })
      "
      icon="i-mdi:pencil"
      v-bind="getAction(BookAction.EDIT_METADATA)"
    />
  </div>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { useIntl } from 'vue-intl'
import { useBookActions } from '@/composables/book/useBookActions'
import { BookAction } from '@/types/book'
import { bookReaderUrl } from '@/api/links'
import { useCurrentUser } from '@/colada/users'
import { useBook } from '@/composables/book/useBook'

const intl = useIntl()

const props = defineProps<{
  book: components['schemas']['BookDto']
}>()

const { isAdmin } = useCurrentUser()
const { isRead, inProgress } = useBookReadProgress(() => props.book)
const { getAction } = useBookActions(() => props.book)
const { canRead, isEpubReader } = useBook(() => props.book)
</script>
