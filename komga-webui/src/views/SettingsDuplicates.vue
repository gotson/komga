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
      <template v-slot:item.name="{ item }">
        <router-link :to="{name:'browse-book', params: {bookId: item.id}}">{{ item.name }}</router-link>
      </template>
    </v-data-table>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {MediaStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {convertErrorCodes} from '@/functions/error-codes'

export default Vue.extend({
  name: 'SettingsDuplicates',
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
      ]
    },
  },
  async mounted() {
    this.loadBooks()
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
  },
})
</script>

<style scoped>

</style>
