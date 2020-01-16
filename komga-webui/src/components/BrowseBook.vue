<template>
  <div v-if="!$_.isEmpty(book)">
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <!--   Go back to parent series   -->
      <v-btn icon
             title="Go to series"
             :to="{name:'browse-series', params: {seriesId: book.seriesId}}"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>

      <v-spacer/>

      <!--   Action menu   -->
      <v-menu offset-y v-if="isAdmin">
        <template v-slot:activator="{ on }">
          <v-btn icon v-on="on">
            <v-icon>mdi-dots-vertical</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item @click="analyze()">
            <v-list-item-title>Analyze</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-toolbar>

    <v-container fluid class="pa-6">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <v-hover>
            <template v-slot:default="{ hover }">
              <v-img :src="thumbnailUrl"
                     lazy-src="../assets/cover.svg"
                     max-height="300"
                     max-width="212"
              >
                <v-fade-transition>
                  <v-overlay
                    v-if="hover && book.media.status === 'READY'"
                    absolute
                    color="grey darken-4"
                  >
                    <v-btn fab
                           x-large
                           color="accent"
                           :to="{name: 'read-book', params: { bookId: bookId}}"
                    >
                      <v-icon>mdi-book-open-page-variant</v-icon>
                    </v-btn>
                  </v-overlay>
                </v-fade-transition>
              </v-img>
            </template>
          </v-hover>

        </v-col>
        <v-col cols="8" sm="8" md="auto" lg="auto" xl="auto">
          <div class="headline">{{ book.name }}</div>
          <div>#{{ book.number }}</div>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="auto">
          <v-btn icon
                 title="Download file"
                 class="pb-1"
                 :href="fileUrl">
            <v-icon>mdi-file-download</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn icon
                 color="primary"
                 title="Read book"
                 class="pb-1"
                 :to="{name: 'read-book', params: { bookId: bookId}}"
                 :disabled="book.media.status !== 'READY'"
          >
            <v-icon>mdi-book-open-page-variant</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-icon class="mr-2 pb-1">mdi-book-open</v-icon>
          <span class="body-2">{{ book.media.pagesCount }} pages</span>
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

      <v-row v-if="book.media.comment">
        <v-col cols="2" md="1" lg="1" xl="1" class="body-2">COMMENT</v-col>
        <v-col cols="10" class="body-2">
          <span class="error--text font-weight-bold">{{ book.media.comment }}</span>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="2" md="1" lg="1" xl="1" class="body-2">FORMAT</v-col>
        <v-col cols="10" class="body-2">
          <span>{{ format.type }}</span>
        </v-col>
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
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    thumbnailUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.bookId}/thumbnail`
    },
    fileUrl (): string {
      return `${this.baseURL}/api/v1/books/${this.bookId}/file`
    },
    format (): BookFormat {
      return getBookFormatFromMediaType(this.book.media.mediaType)
    },
    barStyle (): any {
      if (this.$vuetify.breakpoint.name === 'xs') {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    }
  },
  methods: {
    analyze () {
      this.$komgaBooks.analyzeBook(this.book)
    }
  }
})
</script>

<style scoped>
@import "../assets/css/sticky-bar.css";
</style>
