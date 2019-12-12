<template>
  <div>
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <v-btn icon
             title="Go to series"
             :to="{name:'browse-series', params: {seriesId: book.seriesId}}"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
    </v-toolbar>

    <v-container fluid class="ma-3" v-if="!$_.isEmpty(book)">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <v-img :src="thumbnailUrl"
                 lazy-src="../assets/cover.svg"
                 max-height="300"
                 max-width="212"
          />

        </v-col>
        <v-col cols="8" sm="8" md="auto" lg="auto" xl="auto">
          <div class="headline">{{ book.name }}</div>
          <div>#{{ book.number }}</div>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="1">
          <v-btn icon
                 title="Download file"
                 class="pb-1"
                 :href="fileUrl">
            <v-icon>mdi-file-download</v-icon>
          </v-btn>
        </v-col>
        <v-col>
          <v-icon class="mr-2 pb-1">mdi-book-open-page-variant</v-icon>
          <span class="body-2">{{ book.metadata.pagesCount }} pages</span>
        </v-col>
      </v-row>

      <!--    <v-row>-->
      <!--      <v-col>-->
      <!--        <div class="body-1">Description will go here-->
      <!--        </div>-->
      <!--      </v-col>-->
      <!--    </v-row>-->

      <v-row>
        <v-col cols="2" md="1" lg="1" xl="1" class="body-2">SIZE</v-col>
        <v-col cols="10" class="body-2">{{ book.size }}</v-col>
      </v-row>

      <v-row>
        <v-col cols="2" md="1" lg="1" xl="1" class="body-2">FORMAT</v-col>
        <v-col cols="10" class="body-2">{{ format.type }}</v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="2" md="1" lg="1" xl="1" class="body-2">FILE</v-col>
        <v-col cols="10" class="body-2">{{ book.url }}</v-col>
      </v-row>

    </v-container>
  </div>
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
      this.book = await this.$komgaBooks.getBook(Number(to.params.bookId))
    }

    next()
  },
  computed: {
    thumbnailUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.bookId}/thumbnail`
    },
    fileUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.bookId}/file`
    },
    format (): BookFormat {
      return getBookFormatFromMediaType(this.book.metadata.mediaType)
    },
    barStyle (): any {
      if (this.$vuetify.breakpoint.name === 'xs') {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    }
  }
})
</script>

<style scoped>
@import "../assets/css/sticky-bar.css";
</style>
