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
        <v-list-item @click="promptDeleteSeries" class="list-danger" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.delete') }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import {SeriesDto} from '@/types/komga-series'
import {BookSearch, SearchConditionSeriesId, SearchOperatorIs} from '@/types/komga-search'

export default Vue.extend({
  name: 'SeriesActionsMenu',
  data: () => {
    return {
      menuState: false,
    }
  },
  props: {
    series: {
      type: Object as () => SeriesDto,
      required: true,
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
      return this.series.booksReadCount === this.series.booksCount
    },
    isUnread(): boolean {
      return this.series.booksUnreadCount === this.series.booksCount
    },
  },
  methods: {
    analyze() {
      this.$komgaSeries.analyzeSeries(this.series)
    },
    refreshMetadata() {
      this.$komgaSeries.refreshMetadata(this.series)
    },
    addToCollection() {
      this.$store.dispatch('dialogAddSeriesToCollection', [this.series.id])
    },
    async addToReadList() {
      const books = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionSeriesId(new SearchOperatorIs(this.series.id)),
      } as BookSearch, {unpaged: true, sort: ['metadata.numberSort']})
      this.$store.dispatch('dialogAddBooksToReadList', books.content.map(b => b.id))
    },
    async markRead() {
      await this.$komgaSeries.markAsRead(this.series.id)
    },
    async markUnread() {
      await this.$komgaSeries.markAsUnread(this.series.id)
    },
    promptDeleteSeries() {
      this.$store.dispatch('dialogDeleteSeries', this.series)
    },
  },
})
</script>
