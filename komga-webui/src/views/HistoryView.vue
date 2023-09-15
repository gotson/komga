<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="items"
      :options.sync="options"
      :server-items-length="totalElements"
      :loading="loading"
      sort-by="timestamp"
      :sort-desc="true"
      multi-sort
      class="elevation-1"
      :footer-props="{
        itemsPerPageOptions: [20, 50, 100]
      }"
    >
      <template v-slot:item.type="{ item }">
        <v-icon :title="$t(`enums.historical_event_type.${item.type}`)">{{ getIcon(item.type) }}</v-icon>
      </template>

      <template v-slot:item.seriesId="{ item }">
        <router-link v-if="getSeries(item.seriesId)"
                     :to="{name: 'browse-series', params: {seriesId: item.seriesId}}"
                     class="link-underline"
        >{{ getSeries(item.seriesId).metadata.title }}
        </router-link>
        <template v-else>{{ item.seriesId }}</template>
      </template>

      <template v-slot:item.bookId="{ item }">
        <router-link v-if="getBook(item.bookId)"
                     :to="{name: 'browse-book', params: {bookId: item.bookId}}"
                     class="link-underline"
        >{{ getBook(item.bookId).metadata.title }}
        </router-link>
        <template v-else>{{ item.bookId }}</template>
      </template>

      <template v-slot:item.timestamp="{ item }">
        {{
          new Intl.DateTimeFormat($i18n.locale, {
            dateStyle: 'medium',
            timeStyle: 'short'
          }).format(item.timestamp)
        }}
      </template>

      <template v-slot:item.properties="{ item }">
        <v-btn icon small @click="showDetails(item)">
          <v-icon small>mdi-information</v-icon>
        </v-btn>
      </template>

      <template v-slot:footer.prepend>
        <v-btn icon @click="loadData">
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
      </template>

    </v-data-table>

    <v-dialog
      v-model="dialogDetails"
      scrollable
    >
      <v-card v-if="dialogDetailsItem">
        <v-card-title>{{ $t(`enums.historical_event_type.${dialogDetailsItem.type}`) }}</v-card-title>
        <v-card-text>
          <v-simple-table>
            <tbody>
            <tr v-for="[key, value] in Object.entries(dialogDetailsItem.properties)" :key="key">
              <td class="text-capitalize font-weight-bold">{{ key }}</td>
              <td>{{ value }}</td>
            </tr>
            <tr v-if="getPageHash(dialogDetailsItem)">
              <td class="font-weight-bold">Page</td>
              <td>
                <v-img
                  width="200"
                  height="300"
                  contain
                  :src="pageHashKnownThumbnailUrl(getPageHash(dialogDetailsItem))"
                />
              </td>
            </tr>
            </tbody>
          </v-simple-table>
        </v-card-text>
        <v-card-actions>
          <v-spacer/>
          <v-btn @click="dialogDetails = false" text>{{ $t('common.close') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {HistoricalEventDto} from '@/types/komga-history'
import {SeriesDto} from '@/types/komga-series'
import {BookDto} from '@/types/komga-books'
import {pageHashKnownThumbnailUrl} from '@/functions/urls'
import {PageHashKnownDto} from '@/types/komga-pagehashes'

export default Vue.extend({
  name: 'HistoryView',
  data: function () {
    return {
      pageHashKnownThumbnailUrl,
      items: [] as HistoricalEventDto[],
      totalElements: 0,
      loading: true,
      options: {} as any,
      dialogDetails: false,
      dialogDetailsItem: undefined as HistoricalEventDto | undefined,
      seriesCache: [] as SeriesDto[],
      seriesCacheNotFound: [] as string[],
      booksCache: [] as BookDto[],
      booksCacheNotFound: [] as string[],
    }
  },
  watch: {
    options: {
      handler() {
        this.loadData()
      },
      deep: true,
    },
  },
  computed: {
    headers(): object[] {
      return [
        {text: this.$t('history.header.type').toString(), value: 'type'},
        {text: this.$t('history.header.series').toString(), value: 'seriesId'},
        {text: this.$t('history.header.book').toString(), value: 'bookId'},
        {text: this.$t('history.header.date').toString(), value: 'timestamp'},
        {text: this.$t('history.header.details').toString(), value: 'properties', sortable: false},
      ]
    },
  },
  methods: {
    getPageHash(item: HistoricalEventDto): PageHashKnownDto | undefined {
      if (item.type !== 'DuplicatePageDeleted') return undefined
      let size: any = item.properties['page file size' as any]
      if (size === 'null') size = -1
      return {
        hash: item.properties['page file hash' as any],
        size: size,
        mediaType: item.properties['page media type' as any],
      } as any
    },
    getSeries(seriesId: string): SeriesDto | undefined {
      return this.seriesCache.find(x => x.id === seriesId)
    },
    getBook(bookId: string): BookDto | undefined {
      return this.booksCache.find(x => x.id === bookId)
    },
    showDetails(item: HistoricalEventDto) {
      this.dialogDetailsItem = item
      this.dialogDetails = true
    },
    getIcon(type: string): string {
      switch (type) {
        case 'BookFileDeleted':
          return 'mdi-file-remove'
        case 'SeriesFolderDeleted':
          return 'mdi-folder-remove'
        case 'DuplicatePageDeleted':
          return 'mdi-book-minus'
        case 'BookConverted':
          return 'mdi-archive-refresh'
        case 'BookImported':
          return 'mdi-import'
        default:
          return ''
      }
    },
    async loadData() {
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

      const itemsPage = await this.$komgaHistory.getAll(pageRequest)
      this.totalElements = itemsPage.totalElements
      this.items = itemsPage.content

      for (const seriesId of new Set(this.items.map(x => x.seriesId))) {
        if (seriesId && !this.seriesCacheNotFound.includes(seriesId) && !this.getSeries(seriesId)) {
          this.$komgaSeries.getOneSeries(seriesId)
            .then(s => this.seriesCache.push(s))
            .catch(() => this.seriesCacheNotFound.push(seriesId))
        }
      }

      for (const bookId of new Set(this.items.map(x => x.bookId))) {
        if (bookId && !this.booksCacheNotFound.includes(bookId) && !this.getBook(bookId)) {
          this.$komgaBooks.getBook(bookId)
            .then(b => this.booksCache.push(b))
            .catch(() => this.booksCacheNotFound.push(bookId))
        }
      }

      this.loading = false
    },
  },
})
</script>

<style scoped>

</style>
