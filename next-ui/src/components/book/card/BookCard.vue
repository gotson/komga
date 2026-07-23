<template>
  <ItemCard
    :id="id"
    :title="titleAndLines.title"
    :lines="titleAndLines.lines"
    :poster-url="bookPosterUrl(book.id, cacheStore.getVersion(book.id))"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    :progress-percent="progressPercent"
    :fab-icon="canRead ? 'i-mdi:play' : undefined"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    :card-to="`/book/${book.id}`"
    v-bind="propsLeft"
    @selection="(val, event) => emit('selection', val, event)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="bottomSheet = true"
    @click-fab="openReader"
  />
  <BookMenuSheet
    v-model="bottomSheet"
    :book="book"
    :activator="menuActivator"
  />
</template>

<script setup lang="ts">
import { bookPosterUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import { MediaStatus, mediaStatusMessages } from '@/types/MediaStatus'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { useEditBookMetadataDialog } from '@/composables/book/useEditBookMetadataDialog'
import { useBook } from '@/composables/book/useBook'
import { bookReaderUrl } from '@/api/links'
import type { BookDto } from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'

const intl = useIntl()
const cacheStore = useImageCacheStore()

const id = useId()

const props = defineProps<
  {
    book: BookDto
    showSeries: boolean
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const book = toRef(props, 'book')
const propsLeft = computed(() => {
  const { book, showSeries, ...rest } = props
  return rest
})
const { isRead, progressPercent } = useBookReadProgress(book)

const titleAndLines = computed<{ title: ItemCardTitle; lines: ItemCardLine[] }>(() => {
  let footer: ItemCardLine

  if (book.value.deleted)
    footer = {
      text: intl.formatMessage({
        description: 'Book card subtitle: unavailable',
        defaultMessage: 'Unavailable',
        id: 'nhrFtV',
      }),
      classes: 'text-error',
    }
  else if (book.value.media.status === MediaStatus.Error)
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Error]),
      classes: 'text-error',
    }
  else if (book.value.media.status === MediaStatus.Unsupported)
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Unsupported]),
      classes: 'text-warning',
    }
  else if (book.value.media.status === MediaStatus.Unknown)
    footer = {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Unknown]),
    }
  else
    footer = {
      text: intl.formatMessage(
        {
          description: 'Book card subtitle: count of pages',
          defaultMessage: `{count, plural,
one {# page}
other {# pages}
}`,
          id: 'Ai7bBV',
        },
        { count: book.value.media.pagesCount },
      ),
    }

  if (book.value.oneshot) {
    return {
      title: { text: book.value.metadata.title, lines: 2, routerLink: `/book/${book.value.id}` },
      lines: [footer],
    }
  } else {
    const numberedTitle = `${book.value.metadata.number} - ${book.value.metadata.title}`
    if (props.showSeries)
      return {
        title: {
          text: book.value.seriesTitle,
          lines: 1,
          routerLink: `/series/${book.value.seriesId}`,
        },
        lines: [{ text: numberedTitle, lines: 1, routerLink: `/book/${book.value.id}` }, footer],
      }
    else
      return {
        title: { text: numberedTitle, lines: 2, routerLink: `/book/${book.value.id}` },
        lines: [footer],
      }
  }
})

const { isAdmin } = useCurrentUser()
const { canRead, isEpubReader } = useBook(book)
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
  prepareEditBookMetadataDialog(book.value)
  showEditBookMetadataDialog()
}

const menuActivator = ref()

function openReader() {
  window.open(bookReaderUrl(book.value.id, isEpubReader.value), '_blank')
}
</script>
