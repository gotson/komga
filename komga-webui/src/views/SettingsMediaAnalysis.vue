<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="books"
      :options.sync="options"
      :server-items-length="totalBooks"
      :loading="loading"
      sort-by="media.status"
      multi-sort
      class="elevation-1"
      :footer-props="{
        itemsPerPageOptions: [20, 50, 100]
      }"
    >
      <template v-slot:item.name="{ item }">
        <router-link :to="{name:'browse-book', params: {bookId: item.id}}">{{ item.name }}</router-link>
      </template>
    </v-data-table>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import { MediaStatus } from '@/types/enum-books'

export default Vue.extend({
  name: 'SettingsMediaAnalysis',
  data: () => ({
    books: [] as BookDto[],
    totalBooks: 0,
    loading: true,
    options: {} as any,
    headers: [
      { text: 'Name', value: 'name' },
      { text: 'Status', value: 'media.status' },
      { text: 'Comment', value: 'media.comment' },
      { text: 'Media Type', value: 'media.mediaType' },
      { text: 'URL', value: 'url' },
      { text: 'Size', value: 'size', sortable: false }
    ]
  }),
  watch: {
    options: {
      handler () {
        this.loadBooks()
      },
      deep: true
    }
  },
  computed: {},
  async mounted () {
    this.loadBooks()
  },
  methods: {
    async loadBooks () {
      this.loading = true

      const { sortBy, sortDesc, page, itemsPerPage } = this.options

      const pageRequest = {
        page: page - 1,
        size: itemsPerPage,
        sort: []
      } as PageRequest

      for (let i = 0; i < sortBy.length; i++) {
        pageRequest.sort!!.push(`${sortBy[i]},${sortDesc[i] ? 'desc' : 'asc'}`)
      }

      const booksPage = await this.$komgaBooks.getBooks(undefined, pageRequest, undefined, [MediaStatus.ERROR, MediaStatus.UNSUPPORTED])
      this.totalBooks = booksPage.totalElements
      this.books = booksPage.content

      this.loading = false
    }
  }
})
</script>

<style scoped>

</style>
