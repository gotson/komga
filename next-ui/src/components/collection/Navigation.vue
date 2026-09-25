<template>
  <div
    v-if="shouldShow"
    class="d-flex ga-2"
  >
    <v-btn
      v-ktooltip:bottom="
        $formatMessage({
          description: 'Navigation within collection: previous button tootlip',
          defaultMessage: 'Go to previous series within collection',
          id: 'DgXStw',
        })
      "
      :icon="isRtl ? 'i-mdi:chevron-right' : 'i-mdi:chevron-left'"
      :disabled="!previous"
      :to="{
        name: '/series/[id]',
        params: { id: previous?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />

    <v-menu
      max-height="50vh"
      max-width="350px"
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
          v-for="s in siblings?.content"
          :key="s.id"
          lines="two"
          :title="s.metadata.title"
          :prepend-avatar="seriesPosterUrl(s.id, cacheStore.getVersion(s.id))"
          :append-icon="seriesId === s.id ? 'i-mdi:check-bold' : 'none'"
          :active="seriesId === s.id"
          :to="{
            name: '/series/[id]',
            params: { id: s.id },
            query: formatBrowsingContextAsQueryParam(toValue(context)),
          }"
        />
      </v-list>
    </v-menu>

    <v-btn
      v-ktooltip:bottom="
        $formatMessage({
          description: 'Navigation within collection: next button tootlip',
          defaultMessage: 'Go to next series within collection',
          id: '5Zw5Zt',
        })
      "
      :icon="isRtl ? 'i-mdi:chevron-left' : 'i-mdi:chevron-right'"
      :disabled="!next"
      :to="{
        name: '/series/[id]',
        params: { id: next?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />
  </div>
</template>

<script setup lang="ts">
import { useRtl } from 'vuetify/framework'
import { formatBrowsingContextAsQueryParam, popBrowsingContext } from '@/functions/browsing-context'
import { useBrowsingContext } from '@/composables/browsingContext'
import { seriesPosterUrl } from '@/api/images'
import { useImageCacheStore } from '@/stores/image-cache'
import { useQuery } from '@pinia/colada'
import { collectionDetailQuery } from '@/colada/collections'
import { seriesListQuery } from '@/colada/series'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { findAdjacent } from '@/functions/array'

const props = defineProps<{
  seriesId: string
}>()

const { isRtl } = useRtl()
const cacheStore = useImageCacheStore()

const { context } = useBrowsingContext()
const top = computed(() => popBrowsingContext(context.value)?.top)

// retrieve collection and its series
const { data: collection } = useQuery(() => ({
  ...collectionDetailQuery({ collectionId: top.value?.id || 'none' }),
  enabled: top.value?.type === 'collection',
}))
const sort = computed<Sort[]>(() => {
  if (collection.value?.ordered) return [{ key: 'collection.number', order: 'asc' }]
  else return [{ key: 'metadata.titleSort', order: 'asc' }]
})

const { data: siblings, isPending: siblingsLoading } = useQuery(() => ({
  ...seriesListQuery({
    search: {
      condition: {
        collectionId: {
          operator: 'Is',
          value: collection.value?.id,
        },
      },
    },
    pageRequest: PageRequest.Unpaged(sort.value),
  }),
}))

const previous = computed(() =>
  findAdjacent(siblings.value?.content, (element) => element.id === props.seriesId, 'before'),
)
const next = computed(() =>
  findAdjacent(siblings.value?.content, (element) => element.id === props.seriesId, 'after'),
)

const shouldShow = computed(() => top.value?.type === 'collection')
</script>
