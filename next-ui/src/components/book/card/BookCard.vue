<template>
  <ItemCard
    :id="id"
    :title="titleAndLines.title"
    :lines="titleAndLines.lines"
    :poster-url="bookThumbnailUrl(book.id)"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    :progress-percent="isRead ? undefined : progressPercent"
    fab-icon="i-mdi:play"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    v-bind="props"
    @selection="(val) => emit('selection', val)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="bottomSheet = true"
  />
  <BookMenu
    :book="book"
    :activator="menuActivator"
  />
  <BookMenuBottomSheet
    v-model="bottomSheet"
    :book="book"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { bookThumbnailUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import { MediaStatus, mediaStatusMessages } from '@/types/MediaStatus'

import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { useEditBookMetadataDialog } from '@/composables/book/useEditBookMetadataDialog'

const intl = useIntl()

const id = useId()

const {
  book,
  showSeries = true,
  ...props
} = defineProps<
  {
    book: components['schemas']['BookDto']
    showSeries?: boolean
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const isRead = computed(() => book.readProgress?.completed)
const progressPercent = useBookReadProgress(book)

const titleAndLines = computed<{ title: ItemCardTitle; lines: ItemCardLine[] }>(() => {
  let footer: ItemCardLine

  if (book.deleted)
    footer = {
      text: intl.formatMessage({
        description: 'Book card subtitle: unavailable',
        defaultMessage: 'Unavailable',
        id: 'nhrFtV',
      }),
      classes: 'text-error',
    }
  else if (book.media.status === MediaStatus.ERROR.valueOf())
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.ERROR]),
      classes: 'text-error',
    }
  else if (book.media.status === MediaStatus.UNSUPPORTED.valueOf())
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.UNSUPPORTED]),
      classes: 'text-warning',
    }
  else if (book.media.status === MediaStatus.UNKNOWN.valueOf())
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.UNKNOWN]),
    }
  else
    footer = {
      text: intl.formatMessage(
        {
          description: 'Book card subtitle: count of pages',
          defaultMessage: '{count} pages',
          id: 'BSFC7R',
        },
        { count: book.media.pagesCount },
      ),
    }

  if (book.oneshot) {
    return {
      title: { text: book.metadata.title, lines: 2 },
      lines: [footer],
    }
  } else {
    const numberedTitle = `${book.metadata.number} - ${book.metadata.title}`
    if (showSeries)
      return {
        title: { text: book.seriesTitle, lines: 1 },
        lines: [{ text: numberedTitle, lines: 1 }, footer],
      }
    else return { title: { text: numberedTitle, lines: 2 }, lines: [footer] }
  }
})

const { isAdmin } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
const quickActionProps = computed(() => ({
  id: `${id}_quick`,
  onmouseenter: () => (editMetadataActivator.value = `#${id}_quick`),
}))
const menuIcon = computed(() => (isAdmin.value ? 'i-mdi:dots-vertical' : undefined))
const menuProps = computed(() => ({
  onmouseenter: (event: Event) => (menuActivator.value = event.currentTarget as Element),
}))

const {
  prepareDialog: prepareEditBookMetadataDialog,
  showDialog: showEditBookMetadataDialog,
  activator: editMetadataActivator,
} = useEditBookMetadataDialog()

function showEditMetadataDialog() {
  prepareEditBookMetadataDialog(book)
  showEditBookMetadataDialog()
}

const menuActivator = ref()
</script>
