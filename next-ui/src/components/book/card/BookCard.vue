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
    :card-to="linkTo"
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
    add-select-action
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
import { BrowsingContextKey, formatBrowsingContextAsQueryParam } from '@/functions/browsing-context'
import type { RouteLocationObject } from '@/types/route'
import type { SortKeysBook } from '@/types/sort'
import { commonMessages } from '@/utils/i18n/common-messages'
import { getFileSize } from '@/functions/filesize'

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

const excludedKeys = ['series', 'metadata.numberSort', 'metadata.title', 'pagesCount'] as const
type SortKeysSupported = Exclude<SortKeysBook, (typeof excludedKeys)[number]>
const footer = computed(() => {
  if (book.value.deleted)
    return {
      text: intl.formatMessage({
        description: 'Book card subtitle: unavailable',
        defaultMessage: 'Unavailable',
        id: 'nhrFtV',
      }),
      classes: 'text-error',
    }
  if (book.value.media.status === MediaStatus.Error)
    return {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Error]),
      classes: 'text-error',
    }
  if (book.value.media.status === MediaStatus.Unsupported)
    return {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Unsupported]),
      classes: 'text-warning',
    }
  if (book.value.media.status === MediaStatus.Unknown)
    return {
      text: intl.formatMessage(mediaStatusMessages[MediaStatus.Unknown]),
    }

  const sortKey = props.sortActive?.find(
    (it) => !(excludedKeys as readonly string[]).includes(it.key),
  )
  if (sortKey) {
    switch (sortKey.key as SortKeysSupported) {
      case 'createdDate':
        return {
          text: intl.formatDate(book.value.created, { dateStyle: 'medium' }),
        }
      case 'lastModifiedDate':
        return {
          text: intl.formatDate(book.value.lastModified, {
            dateStyle: 'medium',
          }),
        }
      case 'metadata.releaseDate':
        return {
          text: book.value.metadata.releaseDate
            ? intl.formatDate(book.value.metadata.releaseDate, {
                dateStyle: 'medium',
              })
            : intl.formatMessage(commonMessages.cardSubtitleNoReleaseDate),
        }
      case 'readDate':
        return {
          text: book.value.readProgress
            ? intl.formatDate(book.value.readProgress.readDate, {
                dateStyle: 'medium',
              })
            : intl.formatMessage(commonMessages.cardSubtitleUnread),
        }
      case 'fileSize':
        return { text: getFileSize(book.value.sizeBytes) }
      case 'name':
        return { text: book.value.name }
    }
  }

  return {
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
})

const titleAndLines = computed<{ title: ItemCardTitle; lines: ItemCardLine[] }>(() => {
  if (book.value.oneshot) {
    return {
      title: { text: book.value.metadata.title, lines: 2, routerLink: linkTo.value },
      lines: [footer.value],
    }
  } else {
    const numberedTitle = `${book.value.metadata.number} - ${book.value.metadata.title}`
    if (props.showSeries)
      return {
        title: {
          text: book.value.seriesTitle,
          lines: 1,
          routerLink: {
            name: '/series/[id]',
            params: { id: book.value.seriesId },
            query: formatBrowsingContextAsQueryParam(toValue(context)),
          },
        },
        lines: [{ text: numberedTitle, lines: 1, routerLink: linkTo.value }, footer.value],
      }
    else
      return {
        title: { text: numberedTitle, lines: 2, routerLink: linkTo.value },
        lines: [footer.value],
      }
  }
})

const context = inject(BrowsingContextKey, undefined)
const linkTo = computed<RouteLocationObject>(() => ({
  name: '/book/[id]',
  params: { id: book.value.id },
  query: formatBrowsingContextAsQueryParam(toValue(context)),
}))

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
