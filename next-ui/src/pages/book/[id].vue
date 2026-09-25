<template>
  <v-app-bar>
    <template #prepend>
      <NavigationBreadcrumbs />
    </template>

    <template #append>
      <!-- The relevant navigation will be displayed depending on browsing context -->
      <BookNavigation :book-id="bookId" />
      <CollectionNavigation
        v-if="book?.oneshot"
        :series-id="book.seriesId"
      />
    </template>
  </v-app-bar>

  <v-container
    fluid
    class="pa-0 pa-sm-4"
  >
    <div
      v-if="isPending"
      class="pa-4 pa-sm-0"
    >
      <v-row>
        <v-col cols="3">
          <v-skeleton-loader type="image" />
        </v-col>
        <v-col>
          <v-skeleton-loader type="article" />
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-skeleton-loader type="table-heading@5" />
        </v-col>
      </v-row>
    </div>

    <EmptyStateNetworkError v-else-if="error" />

    <template v-else-if="book">
      <BookView
        :book="book"
        :one-shot-attributes="book.oneshot ? series?.metadata : undefined"
      />
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useQuery } from '@pinia/colada'
import { bookDetailQuery } from '@/colada/books'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import BookView from '../../components/book/view/BookView.vue'
import { seriesDetailQuery } from '@/colada/series'
import { useBrowsingContext } from '@/composables/browsingContext'
import { popBrowsingContext, pushBrowsingContext } from '@/functions/browsing-context'

const route = useRoute('/book/[id]')
const bookId = computed(() => route.params.id)

const {
  data: book,
  error,
  isPending,
} = useQuery(() => ({
  ...bookDetailQuery({ bookId: bookId.value }),
}))

const { data: series } = useQuery(() => ({
  ...seriesDetailQuery({ seriesId: book.value?.seriesId ?? '' }),
  enabled: book.value && book.value.oneshot,
}))

// if the top context is a library, and if the book is not a oneshot, add the parent series as context
const { context } = useBrowsingContext()
watch(
  [context, book],
  ([ctx, b]) => {
    if (ctx && b) {
      const { top } = popBrowsingContext(ctx)
      if (top?.type === 'libraryView' && !b.oneshot) {
        context.value = pushBrowsingContext(ctx, { type: 'series', id: b.seriesId })
      }
    }
  },
  {
    immediate: true,
    deep: true,
  },
)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
