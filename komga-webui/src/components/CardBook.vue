<template>
  <v-card :width="width"
          :to="{name:'browse-book', params: {bookId: book.id}}"
  >
    <v-img
      :src="thumbnailUrl"
      lazy-src="../assets/cover.svg"
      aspect-ratio="0.7071"
    >
      <span class="white--text pa-1 px-2 subtitle-2"
            :style="{background: format.color, position: 'absolute', right: 0}"
      >
        {{ format.type }}
      </span>
    </v-img>

    <v-card-subtitle class="pa-2 pb-1 text--primary"
                     v-line-clamp="2"
                     style="word-break: normal !important;"
                     :title="book.name"
    >
      {{ book.name }}
    </v-card-subtitle>

    <v-card-text class="px-2"
    >
      <div>{{ book.size }}</div>
      <div v-if="book.metadata.pagesCount === 1">{{ book.metadata.pagesCount }} page</div>
      <div v-else>{{ book.metadata.pagesCount }} pages</div>
    </v-card-text>

  </v-card>
</template>

<script lang="ts">
import Vue, { PropType } from 'vue'

interface BookFormat {
  type: string,
  color: string
}

export default Vue.extend({
  name: 'CardBook',
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin
    }
  },
  props: {
    book: {
      type: Object as PropType<BookDto>,
      required: true
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150
    }
  },
  computed: {
    thumbnailUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.book.id}/thumbnail`
    },
    format (): BookFormat {
      switch (this.book.metadata.mediaType) {
        case 'application/x-rar-compressed':
          return { type: 'CBR', color: '#03A9F4' }
        case 'application/zip':
          return { type: 'CBZ', color: '#4CAF50' }
        case 'application/pdf':
          return { type: 'PDF', color: '#FF5722' }
        default:
          return { type: '?', color: '#000000' }
      }
    }
  }
})
</script>

<style scoped>
</style>
