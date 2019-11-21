<template>
  <v-container fluid class="ma-3">
    <div>{{ $_.get(book, 'name', '') }}</div>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'BrowseBook',
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin,
      book: {} as BookDto
    }
  },
  async created () {
    this.book = await this.$komgaBooks.getBook(this.bookId)
  },
  props: {
    bookId: {
      type: Number,
      required: true
    }
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      this.book = await this.$komgaBooks.getBook(this.bookId)
    }

    next()
  },
  methods: {
    getThumbnailUrl () {
      return `${this.baseURL}/api/v1/books/${this.book.id}/thumbnail`
    },
    getFormat () {
      switch (this.book.metadata.mediaType) {
        case 'application/x-rar-compressed':
          return 'CBR'
        case 'application/zip':
          return 'CBZ'
        case 'application/pdf':
          return 'PDF'
        default:
          return 'UNKNOWN'
      }
    }
  }
})
</script>

<style scoped>

</style>
