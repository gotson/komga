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

      <span v-if="book.media.status !== 'READY'"
            class="white--text pa-1 px-2 subtitle-2"
            :style="{background: statusColor, position: 'absolute', bottom: 0, width: `${width}px`}"
      >
        {{ book.media.status }}
      </span>
    </v-img>

    <v-card-subtitle class="pa-2 pb-1 text--primary"
                     v-line-clamp="2"
                     style="word-break: normal !important; height: 4em"
                     :title="book.name"
    >
      #{{ book.number }} - {{ book.name }}
    </v-card-subtitle>

    <v-card-text class="px-2"
    >
      <div>{{ book.size }}</div>
      <div v-if="book.media.pagesCount === 1">{{ book.media.pagesCount }} page</div>
      <div v-else>{{ book.media.pagesCount }} pages</div>
    </v-card-text>

  </v-card>
</template>

<script lang="ts">
import { getBookFormatFromMediaType } from '@/functions/book-format'
import Vue, { PropType } from 'vue'

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
      return getBookFormatFromMediaType(this.book.media.mediaType)
    },
    statusColor (): string {
      switch (this.book.media.status) {
        case 'ERROR':
          return 'red'
        case 'UNKOWN':
          return 'grey'
        case 'UNSUPPORTED':
          return 'orange'
        default:
          return 'black'
      }
    }
  }
})
</script>

<style scoped>
</style>
