<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="books"
      :options.sync="options"
      :server-items-length="totalBooks"
      :loading="loading"
      multi-sort
      class="elevation-1"
      :footer-props="{
        itemsPerPageOptions: [20, 50, 100]
      }"
    >
      <template v-slot:item.seriesTitle="{ item }">
        <router-link
          :to="{name: item.oneshot ? 'browse-oneshot' : 'browse-series', params: {bookId: item.id, seriesId: item.seriesId}}">
          {{ item.seriesTitle }}
        </router-link>
      </template>

      <template v-slot:item.metadata.title="{ item }">
        <router-link
          :to="{name: item.oneshot ? 'browse-oneshot' : 'browse-book', params: {bookId: item.id, seriesId: item.seriesId}}">
          {{ item.metadata.title }}
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
        <v-btn icon @click="loadBooks">
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
      </template>
    </v-data-table>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {MediaStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {
  BookSearch,
  PosterMatch,
  SearchConditionAllOfBook,
  SearchConditionMediaStatus,
  SearchConditionPoster,
  SearchOperatorIs,
  SearchOperatorIsNot,
} from '@/types/komga-search'

export default Vue.extend({
  name: 'MissingPosters',
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
        {text: this.$i18n.tc('common.series', 1).toString(), value: 'seriesTitle', sortable: false},
        {text: this.$i18n.t('common.book').toString(), value: 'metadata.title'},
        {text: this.$i18n.t('media_analysis.media_type').toString(), value: 'media.mediaType'},
        {text: this.$i18n.t('media_analysis.url').toString(), value: 'url'},
        {text: '', value: 'deleted', groupable: false, sortable: false},
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

      const booksPage = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAllOfBook([
          new SearchConditionMediaStatus(new SearchOperatorIs(MediaStatus.READY)),
          new SearchConditionPoster(new SearchOperatorIsNot({selected: true} as PosterMatch)),
        ]),
      } as BookSearch, pageRequest)
      this.totalBooks = booksPage.totalElements
      this.books = booksPage.content

      this.loading = false
    },
  },
})
</script>

<style scoped>

</style>
