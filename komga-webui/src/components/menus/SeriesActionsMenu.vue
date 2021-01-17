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
          <v-list-item-title>Analyze</v-list-item-title>
        </v-list-item>
        <v-list-item @click="refreshMetadata" v-if="isAdmin">
          <v-list-item-title>Refresh metadata</v-list-item-title>
        </v-list-item>
        <v-list-item @click="addToCollection" v-if="isAdmin">
          <v-list-item-title>Add to collection</v-list-item-title>
        </v-list-item>
        <v-list-item @click="markRead" v-if="!isRead">
          <v-list-item-title>Mark as read</v-list-item-title>
        </v-list-item>
        <v-list-item @click="markUnread" v-if="!isUnread">
          <v-list-item-title>Mark as unread</v-list-item-title>
        </v-list-item>
        <v-list-item v-if="canDownload" :href="fileUrl">
          <v-list-item-title>Download series</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>
<script lang="ts">
import { SERIES_CHANGED, seriesToEventSeriesChanged } from '@/types/events'
import Vue from 'vue'
import {SeriesDto} from "@/types/komga-series";
import {seriesFileUrl} from "@/functions/urls";

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
    menuState (val) {
      this.$emit('update:menu', val)
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    canDownload (): boolean {
      return this.$store.getters.meFileDownload
    },
    fileUrl (): string {
      return seriesFileUrl(this.series.id)
    },
    isRead (): boolean {
      return this.series.booksReadCount === this.series.booksCount
    },
    isUnread (): boolean {
      return this.series.booksUnreadCount === this.series.booksCount
    },
  },
  methods: {
    analyze () {
      this.$komgaSeries.analyzeSeries(this.series)
    },
    refreshMetadata () {
      this.$komgaSeries.refreshMetadata(this.series)
    },
    addToCollection () {
      this.$store.dispatch('dialogAddSeriesToCollection', this.series)
    },
    async markRead () {
      await this.$komgaSeries.markAsRead(this.series.id)
      this.$eventHub.$emit(SERIES_CHANGED, seriesToEventSeriesChanged(this.series))
    },
    async markUnread () {
      await this.$komgaSeries.markAsUnread(this.series.id)
      this.$eventHub.$emit(SERIES_CHANGED, seriesToEventSeriesChanged(this.series))
    },
  },
})
</script>
