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
import { getReadProgress } from '@/functions/book-progress'
import { ReadStatus } from '@/types/enum-books'
import Vue from 'vue'

export default Vue.extend({
  name: 'BookActionsMenu',
  props: {
    book: {
      type: Object as () => BookDto,
      required: true,
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    isRead (): boolean {
      return getReadProgress(this.book) === ReadStatus.READ
    },
    isUnread (): boolean {
      return getReadProgress(this.book) === ReadStatus.UNREAD
    },
  },
  methods: {
    analyze () {
      this.$komgaBooks.analyzeBook(this.book)
    },
    refreshMetadata () {
      this.$komgaBooks.refreshMetadata(this.book)
    },
    async markRead () {
      const readProgress = { completed: true } as ReadProgressUpdateDto
      await this.$komgaBooks.updateReadProgress(this.book.id, readProgress)
      this.$emit('mark-read', true)
    },
    async markUnread () {
      await this.$komgaBooks.deleteReadProgress(this.book.id)
      this.$emit('mark-unread', true)
    },
  },
})
</script>
