<template>
  <div
    v-if="shouldShow"
    class="d-flex ga-2"
  >
    <v-btn
      v-ktooltip:bottom="messagePrev"
      :icon="isRtl ? 'i-mdi:chevron-right' : 'i-mdi:chevron-left'"
      :disabled="!previous"
      :to="{
        name: '/book/[id]',
        params: { id: previous?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />

    <v-menu
      max-height="50vh"
      max-width="350px"
      @update:model-value="shouldFetchSiblings = true"
    >
      <template #activator="{ props: activatorProps }">
        <v-btn
          v-bind="activatorProps"
          icon="i-mdi:menu"
        />
      </template>

      <v-list color="primary">
        <v-skeleton-loader
          v-if="siblingsLoading"
          type="list-item-avatar-two-line@5"
        />
        <v-list-item
          v-for="b in siblings?.content"
          :key="b.id"
          lines="two"
          :title="b.metadata.title"
          :subtitle="
            forReadList
              ? b.oneshot
                ? undefined
                : b.seriesTitle
              : $formatMessage(
                  {
                    description: 'Book navigation siblings menu: book number in series',
                    defaultMessage: 'Book {number}',
                    id: 'RcbD73',
                  },
                  { number: b.metadata.number },
                )
          "
          :prepend-avatar="bookPosterUrl(b.id, cacheStore.getVersion(b.id))"
          :append-icon="bookId === b.id ? 'i-mdi:check-bold' : 'none'"
          :to="{
            name: '/book/[id]',
            params: { id: b.id },
            query: formatBrowsingContextAsQueryParam(toValue(context)),
          }"
        />
      </v-list>
    </v-menu>

    <v-btn
      v-ktooltip:bottom="messageNext"
      :icon="isRtl ? 'i-mdi:chevron-left' : 'i-mdi:chevron-right'"
      :disabled="!next"
      :to="{
        name: '/book/[id]',
        params: { id: next?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />
  </div>
</template>

<script setup lang="ts">
import { useRtl } from 'vuetify/framework'
import { useBookNavigationFromContext } from '@/composables/book/useBookNavigationFromContext'
import { formatBrowsingContextAsQueryParam, popBrowsingContext } from '@/functions/browsing-context'
import { useIntl } from 'vue-intl'
import { useBookSiblings } from '@/composables/book/useBookSiblings'
import { useBookParentFromContext } from '@/composables/book/useBookParentFromContext'
import { useBrowsingContext } from '@/composables/browsingContext'
import { bookPosterUrl } from '@/api/images'
import { useImageCacheStore } from '@/stores/image-cache'

const props = defineProps<{
  bookId: string
}>()

const intl = useIntl()
const { isRtl } = useRtl()
const cacheStore = useImageCacheStore()

const shouldFetchSiblings = ref(false)

const { context } = useBrowsingContext()
const top = computed(() => popBrowsingContext(context.value)?.top)

const { previous, next } = useBookNavigationFromContext(() => props.bookId, top)
const { parent } = useBookParentFromContext(top, shouldFetchSiblings)
const { data: siblings, isPending: siblingsLoading } = useBookSiblings(parent, shouldFetchSiblings)

const forSeries = computed(() => top.value?.type === 'series')
const forReadList = computed(() => top.value?.type === 'readList')

const shouldShow = computed(() => forSeries.value || forReadList.value)

const messagePrev = computed(() =>
  forReadList.value
    ? intl.formatMessage({
        description: 'Book navigation within read list: previous button tootlip',
        defaultMessage: 'Go to previous book within read list',
        id: 'Xq4kkn',
      })
    : intl.formatMessage({
        description: 'Book navigation within series: previous button tootlip',
        defaultMessage: 'Go to previous book within series',
        id: 'P9B8fE',
      }),
)
const messageNext = computed(() =>
  forReadList.value
    ? intl.formatMessage({
        description: 'Book navigation within read list: next button tootlip',
        defaultMessage: 'Go to next book within read list',
        id: 'Rw2Iwj',
      })
    : intl.formatMessage({
        description: 'Book navigation within series: next button tootlip',
        defaultMessage: 'Go to next book within series',
        id: 'I75Xgh',
      }),
)
</script>
