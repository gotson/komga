<template>
  <div v-if="pages.length > 0">
    <v-btn icon @click="closeBook">
      <v-icon>mdi-close</v-icon>
    </v-btn>

    <v-btn icon @click="prev" :disabled="!canPrev">
      <v-icon>mdi-chevron-left</v-icon>
    </v-btn>

    <v-btn icon @click="next" :disabled="!canNext">
      <v-icon>mdi-chevron-right</v-icon>
    </v-btn>

    <transition name="slide-x-transition">
      <v-img :src="getPageUrl(currentPage)"
             :key="getPageUrl(currentPage)"
             contain
             :max-height="$vuetify.breakpoint.height"
             :max-width="$vuetify.breakpoint.width"
      />
    </transition>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'BookReader',
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin,
      book: {} as BookDto,
      pages: [] as PageDto[],
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
      convertTo: 'png',
      currentPage: 1
    }
  },
  async created () {
    this.book = await this.$komgaBooks.getBook(this.bookId)
    this.pages = await this.$komgaBooks.getBookPages(this.bookId)
  },
  props: {
    bookId: {
      type: Number,
      required: true
    }
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      this.book = await this.$komgaBooks.getBook(Number(to.params.bookId))
    }
    next()
  },
  watch: {
    currentPage (val) {
      this.updateRoute()
      this.preloadNext()
    }
  },
  computed: {
    canPrev (): boolean {
      return this.currentPage > 1
    },
    canNext (): boolean {
      return this.currentPage < this.book.metadata.pagesCount
    }
  },
  methods: {
    getPageUrl (pageNum: number): string {
      const page = this.pages[pageNum - 1]
      let url = `${this.baseURL}/api/v1/books/${this.bookId}/pages/${page.number}`
      if (!this.supportedMediaTypes.includes(page.mediaType)) {
        url += `?convert=${this.convertTo}`
      }
      return url
    },
    prev () {
      if (this.canPrev) {
        this.currentPage -= 1
      }
    },
    next () {
      if (this.canNext) {
        this.currentPage += 1
      }
    },
    preloadNext () {
      if (this.canNext) {
        const img = new Image()
        img.src = this.getPageUrl(this.currentPage + 1)
      }
    },
    updateRoute () {
      this.$router.replace({
        name: this.$route.name,
        params: { bookId: this.$route.params.bookId },
        query: { page: this.currentPage.toString() }
      })
    },
    closeBook () {
      this.$router.back()
    }
  }
})
</script>

<style scoped>

</style>
