<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="booksData"
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
        <router-link
          :to="{name: item.oneshot ? 'browse-oneshot' : 'browse-book', params: {bookId: item.id, seriesId: item.seriesId}}">
          {{ item.name }}
        </router-link>
      </template>

      <template v-slot:item.deleted="{ item }">
        <v-chip
          v-if="item.deleted"
          label small color="error">
          {{ $t('common.unavailable') }}
        </v-chip>
      </template>

      <template v-slot:footer.prepend>
        <v-btn icon @click="loadBooks"><v-icon>mdi-refresh</v-icon></v-btn>
      </template>
    </v-data-table>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {MediaStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {convertErrorCodes} from '@/functions/error-codes'
import {BookSearch, SearchConditionAnyOfBook, SearchConditionMediaStatus, SearchOperatorIs} from '@/types/komga-search'

export default Vue.extend({
  name: 'MediaAnalysis',
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
        {text: this.$i18n.t('media_analysis.name').toString(), value: 'name'},
        {text: this.$i18n.t('media_analysis.status').toString(), value: 'media.status'},
        {text: this.$i18n.t('media_analysis.comment').toString(), value: 'media.comment'},
        {text: this.$i18n.t('media_analysis.media_type').toString(), value: 'media.mediaType'},
        {text: this.$i18n.t('media_analysis.url').toString(), value: 'url'},
        {text: this.$i18n.t('media_analysis.size').toString(), value: 'size'},
        {text: '', value: 'deleted', groupable: false, sortable: false},
      ]
    },
    booksData():BookDto[] {
      return this.books.map((b:BookDto) => ({
        ...b,
        media: {
          ...b.media,
          comment: convertErrorCodes(b.media.comment),
          status: this.$t(`enums.media_status.${b.media.status}`).toString()},
      }))
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

      const booksPage = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAnyOfBook([
          new SearchConditionMediaStatus(new SearchOperatorIs(MediaStatus.ERROR)),
          new SearchConditionMediaStatus(new SearchOperatorIs(MediaStatus.UNSUPPORTED)),
        ]),
      } as BookSearch, pageRequest)
      this.totalBooks = booksPage.totalElements
      this.$store.commit('setBooksToCheck', booksPage.totalElements)
      this.books = booksPage.content

      this.loading = false
    },
  },
})
</script>

<style scoped>

</style>
