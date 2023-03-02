<template>
  <v-snackbar
    v-model="snackbar.show"
    :color="snackbar.color"
    bottom
    multi-line
    :vertical="snackbar.vertical"
    :timeout="snackbar.timeout"
  >
    <p>{{ snackbar.text }}</p>
    <p>{{ snackbar.text2 }}</p>
    <template v-slot:action="{ attrs }">
      <v-btn
        v-if="snackbar.goTo"
        color="secondary"
        text
        v-bind="attrs"
        @click="snackbar.goTo.click(); close()"
      >
        {{ snackbar.goTo.text }}
      </v-btn>

      <v-btn
        text
        v-bind="attrs"
        @click="close"
      >{{ $t('common.dismiss') }}
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script lang="ts">
import Vue from 'vue'
import {BOOK_IMPORTED, ERROR, ErrorEvent, NOTIFICATION, NotificationEvent} from '@/types/events'
import {convertErrorCodes} from '@/functions/error-codes'
import {BookImportSseDto} from '@/types/komga-sse'

export default Vue.extend({
  name: 'ToasterNotification',
  data: function () {
    return {
      queue: [] as any[],
      snackbar: {
        show: false,
        vertical: false,
        text: '',
        text2: '',
        color: undefined,
        timeout: 5000,
        goTo: {
          text: '',
          click: () => {
          },
        },
      },
    }
  },
  created() {
    this.$eventHub.$on(BOOK_IMPORTED, this.onBookImported)
    this.$eventHub.$on(ERROR, this.onError)
    this.$eventHub.$on(NOTIFICATION, this.onNotification)
  },
  beforeDestroy() {
    this.$eventHub.$off(BOOK_IMPORTED, this.onBookImported)
    this.$eventHub.$off(ERROR, this.onError)
    this.$eventHub.$off(NOTIFICATION, this.onNotification)
  },
  watch: {
    'snackbar.show'(val) {
      if (!val) {
        setTimeout(() => this.next(), 1000)
      }
    },
    queue(val) {
      if (val.length > 0) {
        this.next()
      }
    },
  },
  methods: {
    close() {
      this.snackbar.show = false
    },
    next() {
      if (this.snackbar.show) {
        return
      }
      if (this.queue.length > 0) {
        const snack = this.queue.shift()
        this.snackbar.text = snack.text
        this.snackbar.text2 = snack.text2
        this.snackbar.goTo = snack.goTo
        this.snackbar.color = snack.color
        this.snackbar.vertical = snack.text2 !== undefined
        this.snackbar.show = true
      }
    },
    onError(event: ErrorEvent) {
      this.queue.push({
        text: event.message,
        color: 'error',
      })
    },
    onNotification(event: NotificationEvent) {
      this.queue.push({
        text: event.message,
        text2: event.text2,
        goTo: event.goTo,
      })
    },
    async onBookImported(event: BookImportSseDto) {
      if (event.success && event.bookId) {
        const book = await this.$komgaBooks.getBook(event.bookId)
        this.queue.push({
          text: this.$t('book_import.notification.import_successful', {book: book.metadata.title}).toString(),
          text2: this.$t('book_import.notification.source_file', {file: event.sourceFile}).toString(),
          goTo: {
            text: this.$t('book_import.notification.go_to_book').toString(),
            click: () => this.$router.push({name: 'browse-book', params: {bookId: book.id}}),
          },
        })
      } else {
        this.queue.push({
          text: this.$t('book_import.notification.import_failure', {file: event.sourceFile}).toString(),
          text2: convertErrorCodes(event.message || ''),
          color: 'error',
        })
      }
    },
  },
})
</script>

<style scoped>

</style>
