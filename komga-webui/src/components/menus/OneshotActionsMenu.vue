<template>
  <div>
    <v-menu offset-y v-model="menuState">
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item @click="analyze" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.analyze') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="refreshMetadata" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.refresh_metadata') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="addToCollection" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.add_to_collection') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="addToReadList" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.add_to_readlist') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="markRead" v-if="!isRead">
          <v-list-item-title>{{ $t('menu.mark_read') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="markUnread" v-if="!isUnread">
          <v-list-item-title>{{ $t('menu.mark_unread') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="promptDelete" class="list-danger" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.delete') }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>
<script lang="ts">
import {getReadProgress} from '@/functions/book-progress'
import {ReadStatus} from '@/types/enum-books'
import Vue from 'vue'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from '@/types/komga-series'
import {BookSearch, SearchConditionSeriesId, SearchOperatorIs} from '@/types/komga-search'

export default Vue.extend({
  name: 'OneShotActionsMenu',
  data: () => {
    return {
      menuState: false,
      localBookId: undefined as unknown as string,
    }
  },
  props: {
    book: {
      type: Object as () => BookDto,
      required: false,
    },
    series: {
      type: Object as () => SeriesDto,
      required: false,
    },
    menu: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    menuState(val) {
      this.$emit('update:menu', val)
    },
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    isRead(): boolean {
      return this.series ? this.series.booksReadCount === this.series.booksCount : getReadProgress(this.book) === ReadStatus.READ
    },
    isUnread(): boolean {
      return this.series ? this.series.booksUnreadCount === this.series.booksCount : getReadProgress(this.book) === ReadStatus.UNREAD
    },
    seriesId(): string {
      return this.series ? this.series.id : this.book?.seriesId
    },
  },
  methods: {
    analyze() {
      if (this.book) this.$komgaBooks.analyzeBook(this.book)
      else this.$komgaSeries.analyzeSeries(this.series)
    },
    refreshMetadata() {
      if (this.book) this.$komgaBooks.refreshMetadata(this.book)
      this.$komgaSeries.refreshMetadata(this.series)
    },
    addToCollection() {
      this.$store.dispatch('dialogAddSeriesToCollection', [this.seriesId])
    },
    async addToReadList() {
      if (!this.book && !this.localBookId) this.localBookId = (await this.$komgaBooks.getBooksList({
        condition: new SearchConditionSeriesId(new SearchOperatorIs(this.seriesId)),
      } as BookSearch)).content[0].id
      this.$store.dispatch('dialogAddBooksToReadList', [this.book?.id || this.localBookId])
    },
    async markRead() {
      await this.$komgaSeries.markAsRead(this.seriesId)
    },
    async markUnread() {
      await this.$komgaSeries.markAsUnread(this.seriesId)
    },
    promptDelete() {
      if (this.book) this.$store.dispatch('dialogDeleteBook', this.book)
      else this.$store.dispatch('dialogDeleteSeries', this.series)
    },
  },
})
</script>
