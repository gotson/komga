<template>
  <v-breadcrumbs
    :items="items"
    class="px-0 px-sm-3"
  >
    <template #divider>
      <v-icon :icon="isRtl ? 'i-mdi:chevron-left' : 'i-mdi:chevron-right'" />
    </template>

    <template #item="{ item, index }">
      <v-btn
        v-if="index === 0 && display.xs.value"
        icon="i-mdi:arrow-left"
        class="me-1"
        :to="(item as ContextItem).to"
      />
      <div class="d-flex flex-column">
        <RouterLink
          class="text-title-large link-underline d-inline text-truncate"
          :style="{ 'max-width': display.xs.value ? '120px' : '200px' }"
          :to="(item as ContextItem).to"
        >
          <SeriesFetchDetails
            v-if="(item as ContextItem).type === 'series'"
            :series-id="(item as ContextItem).id"
          >
            <template #default="{ details }">
              <span v-ktooltip:bottom-start>{{ details?.metadata.title }}</span>
            </template>
          </SeriesFetchDetails>

          <CollectionFetchDetails
            v-if="(item as ContextItem).type === 'collection'"
            :collection-id="(item as ContextItem).id"
          >
            <template #default="{ details }">
              <span v-ktooltip:bottom-start>{{ details?.name }}</span>
            </template>
          </CollectionFetchDetails>

          <ReadlistFetchDetails
            v-if="(item as ContextItem).type === 'readList'"
            :read-list-id="(item as ContextItem).id"
          >
            <template #default="{ details }">
              <span v-ktooltip:bottom-start>{{ details?.name }}</span>
            </template>
          </ReadlistFetchDetails>

          <LibraryFetchDetails
            v-if="(item as ContextItem).type === 'libraryView'"
            :library-view-id="(item as ContextItem).id"
          >
            <template #default="{ isSingle, library, isAll, isPinned, isUnpinned }">
              <span
                v-if="isSingle"
                v-ktooltip:bottom-start
                >{{ library?.name }}
              </span>
              <span
                v-if="isPinned"
                v-ktooltip:bottom-start
                >{{ $formatMessage(commonMessages.libraryPinned) }}</span
              >
              <span
                v-if="isAll"
                v-ktooltip:bottom-start
                >{{ $formatMessage(commonMessages.libraryAll) }}</span
              >
              <span
                v-if="isUnpinned"
                v-ktooltip:bottom-start
                >{{ $formatMessage(commonMessages.libraryUnpinned) }}</span
              >
            </template>
          </LibraryFetchDetails>
        </RouterLink>

        <span class="text-body-small">{{ getContextSubtitle((item as ContextItem).type) }}</span>
      </div>
    </template>
  </v-breadcrumbs>
</template>

<script setup lang="ts">
import { useBrowsingContext } from '@/composables/browsingContext'
import {
  browsingContextToRouteLocation,
  formatBrowsingContextAsQueryParam,
  type SingleBrowsingContext,
} from '@/functions/browsing-context'
import type { RouteLocationRaw } from 'vue-router'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useRtl } from 'vuetify/framework'
import { useDisplay } from 'vuetify'

const intl = useIntl()
const { isRtl } = useRtl()
const display = useDisplay()

type ContextItem = {
  title: string
  to: RouteLocationRaw
  type: SingleBrowsingContext['type']
  id: string
}

const { context } = useBrowsingContext()

const items = computed(() => {
  const segments = context.value?.map((it, index) => {
    // keep ancestors up to the current item, excluding the item itself
    const ancestorStack = context.value?.slice(0, index)

    return {
      title: `${it.type}_${it.id}`,
      type: it.type,
      id: it.id,
      to: browsingContextToRouteLocation(it, formatBrowsingContextAsQueryParam(ancestorStack)),
    }
  })

  return display.xs.value ? segments?.slice(-1) : segments
})

function getContextSubtitle(type: SingleBrowsingContext['type']): string {
  switch (type) {
    case 'series':
      return intl.formatMessage({
        description: 'Browsing context: series',
        defaultMessage: 'Series',
        id: 'ZGppaw',
      })
    case 'readList':
      return intl.formatMessage({
        description: 'Browsing context: read list',
        defaultMessage: 'Read list',
        id: 'ldAVd7',
      })
    case 'collection':
      return intl.formatMessage({
        description: 'Browsing context: collection',
        defaultMessage: 'Collection',
        id: '3lLmfX',
      })
    case 'libraryView':
      return intl.formatMessage({
        description: 'Browsing context: library',
        defaultMessage: 'Library',
        id: 'pCBNlA',
      })
  }
}
</script>
