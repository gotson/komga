<template>
  <v-container fluid class="ma-3">
    <v-row v-if="!$_.isEmpty(book)">
      <v-col cols="2">
        <v-img :src="thumbnailUrl"
               lazy-src="../assets/cover.svg"
               height="300"
               max-width="212"
        >
          <span class="white--text pa-1 px-2 subtitle-2"
                :style="{background: format.color, position: 'absolute', right: 0}"
          >
            {{ format.type }}
          </span>
        </v-img>
        <v-btn :href="fileUrl" class="mt-2">
          <v-icon left>mdi-file-download</v-icon>
          Download
        </v-btn>
      </v-col>
      <v-col cols="10">
        <div class="title">#{{ book.number }} - {{ book.name }}</div>
        <div class="caption">Filepath: {{ book.url }}</div>
        <div>{{ book.size }}</div>
        <div>{{ book.metadata.pagesCount }} pages</div>

      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { getBookFormatFromMediaType } from '@/functions/book-format'
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
  computed: {
    thumbnailUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.book.id}/thumbnail`
    },
    fileUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.book.id}/file`
    },
    format (): BookFormat {
      return getBookFormatFromMediaType(this.book.metadata.mediaType)
    }
  }
})
</script>

<style scoped>

</style>
