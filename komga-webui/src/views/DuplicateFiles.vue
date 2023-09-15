<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="books"
      :options.sync="options"
      :server-items-length="totalBooks"
      :loading="loading"
      sort-by="fileHash"
      multi-sort
      show-group-by
      group-by="fileHash"
      class="elevation-1"
      :footer-props="{
        itemsPerPageOptions: [20, 50, 100]
      }"
    >
      <template v-slot:item.url="{ item }">
        <router-link
          :to="{name: item.oneshot ? 'browse-oneshot' : 'browse-book', params: {bookId: item.id, seriesId: item.seriesId}}">
          {{ item.url }}
        </router-link>
      </template>

      <template v-slot:item.deleted="{ item }">
        <v-chip
          v-if="item.deleted"
          label small color="error">
          {{ $t('common.unavailable') }}
        </v-chip>
      </template>

      <template v-slot:item.id="{ item }">
        <v-btn
          icon
          color="error"
          @click="promptDeleteBook(item)"
        >
          <v-icon>mdi-trash-can-outline</v-icon>
        </v-btn>
      </template>

      <template v-slot:footer.prepend>
        <v-btn icon @click="loadBooks"><v-icon>mdi-refresh</v-icon></v-btn>
      </template>
    </v-data-table>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {BookDto} from '@/types/komga-books'

export default Vue.extend({
  name: 'DuplicateFiles',
  data: function () {
    return {
      books: [] as BookDto[],
      totalBooks: 0,
      loading: true,
      options: {} as any,
    }
  },
  watch: {
    options: {
      handler() {
        this.loadBooks()
      },
      deep: true,
    },
  },
  computed: {
    headers(): object[] {
      return [
        {text: this.$i18n.t('duplicates.file_hash').toString(), value: 'fileHash'},
        {text: this.$i18n.t('duplicates.url').toString(), value: 'url', groupable: false},
        {text: this.$i18n.t('duplicates.size').toString(), value: 'size', groupable: false},
        {text: '', value: 'deleted', groupable: false, sortable: false},
        {text: this.$i18n.t('menu.delete').toString(), value: 'id', groupable: false},
      ]
    },
  },
  methods: {
    async loadBooks() {
      this.loading = true

      const {sortBy, sortDesc, page, itemsPerPage} = this.options

      const pageRequest = {
        page: page - 1,
        size: itemsPerPage,
        sort: [],
      } as PageRequest

      for (let i = 0; i < sortBy.length; i++) {
        pageRequest.sort!!.push(`${sortBy[i]},${sortDesc[i] ? 'desc' : 'asc'}`)
      }

      const booksPage = await this.$komgaBooks.getDuplicateBooks(pageRequest)
      this.totalBooks = booksPage.totalElements
      this.books = booksPage.content

      this.loading = false
    },
    promptDeleteBook(book: BookDto) {
      this.$store.dispatch('dialogDeleteBook', book)
    },
  },
})
</script>

<style scoped>

</style>
