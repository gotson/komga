<template>
  <div>
    <v-menu offset-y>
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list>
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
      </v-list>
    </v-menu>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SeriesActionsMenu',
  props: {
    series: {
      type: Object,
      required: true,
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
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
      this.$emit('add-to-collection', true)
    },
    async markRead () {
      await this.$komgaSeries.markAsRead(this.series.id)
      this.$emit('mark-read', true)
    },
    async markUnread () {
      await this.$komgaSeries.markAsUnread(this.series.id)
      this.$emit('mark-unread', true)
    },
  },
})
</script>
