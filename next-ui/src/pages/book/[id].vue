<template>
  <v-container fluid>
    <div v-if="isPending">
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
      <BookView :book="book" />
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useQuery } from '@pinia/colada'
import { bookDetailQuery } from '@/colada/books'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import BookView from '../../components/book/view/BookView.vue'

const route = useRoute('/book/[id]')
const bookId = computed(() => route.params.id)

const {
  data: book,
  error,
  isPending,
} = useQuery(() => ({
  ...bookDetailQuery({ bookId: bookId.value }),
}))
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
