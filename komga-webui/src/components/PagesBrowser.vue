<template>
  <div v-if="hasPages">
    <v-divider class="my-4" />
    <v-row>
      <v-col class="justify-center">
        <v-pagination v-model="page" :total-visible="pageSize" :length="Math.ceil(pages.length / pageSize)" />
      </v-col>
    </v-row>
    <v-row>
      <v-col class="d-flex flex-wrap">
        <v-card v-for="page in visiblePages" :key="page.number" class="my-2 mx-2" width="150"
          @click="goTo(page.number)">
          <v-img :src="page.thumbnailUrl" aspect-ratio="0.7071" :class="blur ? 'item-card blur' : 'item-card'" style="cursor: pointer" />

          <v-card-subtitle v-line-clamp="2">{{ page.number }}
          </v-card-subtitle>
        </v-card>

      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { bookPageThumbnailUrl, bookPageUrl } from '@/functions/urls'

export default Vue.extend({
  name: 'PagesBrowser',
  props: {
    bookId: {
      type: String,
    },
    readRouteName: {
      type: String,
    },
    totalPages: {
      type: Number,
    },
    pageSize: {
      type: Number,
    },
    blur: {
      type: Boolean,
    },
  },
  data: () => {
    return {
      page: 1,
    }
  },
  computed: {
    hasPages(): boolean {
      return this.totalPages > 0
    },
    pages(): { thumbnailUrl: string; url: string; number: number }[] {
      let pages = []
      for (let p = 1; p <= this.totalPages; p++) {
        pages.push(this.getPage(p))
      }
      return pages
    },
    currentPage(): number {
      if ((this.page - 1) * this.pageSize > this.totalPages)
        return Math.ceil(this.totalPages / this.pageSize)
      else
        return this.page
    },
    visiblePages(): { thumbnailUrl: string; url: string; number: number }[] {
      let start: number = (this.currentPage - 1) * this.pageSize
      let end: number = this.currentPage * this.pageSize
      return this.pages.slice(start, end)
    },
  },
  methods: {
    getPage(pageNumber: number): { thumbnailUrl: string; url: string; number: number } {
      return {
        thumbnailUrl: this.getThumbnailUrl(pageNumber),
        number: pageNumber,
      }
    },
    getThumbnailUrl(pageNumber: number): string {
      return bookPageThumbnailUrl(this.bookId, pageNumber)
    },
    goTo(pageNumber: number): void {
      this.$router.push(
        {
          name: this.readRouteName,
          params: {
            bookId: this.bookId,
          },
          query: {
            page: pageNumber,
            incognito: true,
          },
        })
    },
  },

})
</script>

<style scoped>
.blur > .v-image__image {
  filter: blur(5px);
}
</style>
