<template>
  <div v-if="!$_.isEmpty(book)">
    <toolbar-sticky>
      <!--   Action menu   -->
      <book-actions-menu v-if="book"
                         :book="book"
      />

      <v-btn icon @click="editBook" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>

      <v-spacer/>

      <v-btn
        icon
        :disabled="$_.isEmpty(siblingPrevious)"
        :to="{ name: 'browse-book', params: { bookId: previousId } }"
      >
        <v-icon>mdi-chevron-left</v-icon>
      </v-btn>

      <v-menu bottom
              offset-y
              :max-height="$vuetify.breakpoint.height * .7"
              :max-width="250"
      >
        <template v-slot:activator="{ on }">
          <v-btn icon v-on="on">
            <v-icon>mdi-menu</v-icon>
          </v-btn>
        </template>

        <v-list
          flat
        >
          <v-list-item-group color="primary">
            <v-list-item
              v-for="(book, i) in siblings"
              :key="i"
              :to="{ name: 'browse-book', params: { bookId: book.id } }"
            >
              <v-list-item-title class="text-wrap text-body-2">{{ book.metadata.number }} - {{ book.metadata.title }}
              </v-list-item-title>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-menu>

      <v-btn
        icon
        :disabled="$_.isEmpty(siblingNext)"
        :to="{ name: 'browse-book', params: { bookId: nextId } }"
      >
        <v-icon>mdi-chevron-right</v-icon>
      </v-btn>
    </toolbar-sticky>

    <v-container fluid class="px-6">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <item-card
            v-if="book.hasOwnProperty('id')"
            width="212"
            :item="book"
            thumbnail-only
            no-link
            :action-menu="false"
          ></item-card>
        </v-col>

        <v-col cols="8">
          <v-row>
            <v-col class="py-1">
              <router-link :to="{name:'browse-series', params: {seriesId: book.seriesId}}" class="link-underline">
                <span class="text-h5" v-if="!$_.isEmpty(series)">{{ series.metadata.title }}</span>
              </router-link>
            </v-col>
          </v-row>
          <v-row>
            <v-col class="py-1">
              <div class="text-h6">{{ book.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row class="text-body-2">
            <v-col>
              <span class="mr-3">#{{ book.metadata.number }}</span>
              <badge v-if="book.metadata.ageRating">{{ book.metadata.ageRating }}+</badge>
            </v-col>
            <v-col cols="auto" v-if="book.metadata.releaseDate">
              {{ book.metadata.releaseDate | moment
            ('MMMM DD, YYYY') }}
            </v-col>
          </v-row>

          <v-divider/>

          <v-row class="text-body-2" v-if="book.metadata.publisher">
            <v-col cols="6" sm="4" md="2">PUBLISHER</v-col>
            <v-col>{{ book.metadata.publisher }}</v-col>
          </v-row>

          <v-row class="text-body-2"
                 v-for="(names, key) in authorsByRole"
                 :key="key"
          >
            <v-col cols="6" sm="4" md="2" class="py-1 text-uppercase">{{ key }}</v-col>
            <v-col class="py-1">
              <span v-for="(name, i) in names"
                    :key="name"
              >{{ i === 0 ? '' : ', ' }}{{ name }}</span>
            </v-col>
          </v-row>

          <v-row class="mt-3">
            <v-col>
              <div class="text-body-1"
                   style="white-space: pre-wrap"
              >{{ book.metadata.summary }}
              </div>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="auto">
          <v-btn icon
                 title="Download file"
                 class="pb-1"
                 :disabled="!canDownload"
                 :href="fileUrl">
            <v-icon>mdi-file-download</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn icon
                 color="accent"
                 title="Read book"
                 class="pb-1"
                 :to="{name: 'read-book', params: { bookId: bookId}}"
                 :disabled="book.media.status !== 'READY' || !canReadPages"
          >
            <v-icon>mdi-book-open-page-variant</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-icon class="mr-2 pb-1">mdi-book-open</v-icon>
          <span class="text-body-2">{{ book.media.pagesCount }} pages</span>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="2" md="1" lg="1" xl="1" class="text-body-2">SIZE</v-col>
        <v-col cols="10" class="text-body-2">{{ book.size }}</v-col>
      </v-row>

      <v-row v-if="book.media.comment">
        <v-col cols="2" md="1" lg="1" xl="1" class="text-body-2">COMMENT</v-col>
        <v-col cols="10" class="text-body-2">
          <span class="error--text font-weight-bold">{{ book.media.comment }}</span>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="2" md="1" lg="1" xl="1" class="text-body-2">FORMAT</v-col>
        <v-col cols="10" class="text-body-2">
          <span>{{ format.type }}</span>
        </v-col>
      </v-row>

      <v-row v-if="book.metadata.readingDirection">
        <v-col cols="2" md="1" lg="1" xl="1" class="text-body-2">READING DIRECTION</v-col>
        <v-col cols="10" class="text-body-2">{{ readingDirection }}
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="2" md="1" lg="1" xl="1" class="text-body-2">FILE</v-col>
        <v-col cols="10" class="text-body-2">{{ book.url }}</v-col>
      </v-row>

    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import BookActionsMenu from '@/components/menus/BookActionsMenu.vue'
import ItemCard from '@/components/ItemCard.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import { groupAuthorsByRolePlural } from '@/functions/authors'
import { getBookFormatFromMediaType } from '@/functions/book-format'
import { getReadProgress, getReadProgressPercentage } from '@/functions/book-progress'
import { getBookTitleCompact } from '@/functions/book-title'
import { bookFileUrl, bookThumbnailUrl } from '@/functions/urls'
import { ReadStatus } from '@/types/enum-books'
import { BOOK_CHANGED, LIBRARY_DELETED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'BrowseBook',
  components: { ToolbarSticky, Badge, ItemCard, BookActionsMenu },
  data: () => {
    return {
      book: {} as BookDto,
      series: {} as SeriesDto,
      siblings: [] as BookDto[],
      siblingPrevious: {} as BookDto,
      siblingNext: {} as BookDto,
    }
  },
  async created () {
    this.loadBook(this.bookId)
    this.$eventHub.$on(BOOK_CHANGED, this.reloadBook)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
  },
  beforeDestroy () {
    this.$eventHub.$off(BOOK_CHANGED, this.reloadBook)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
  },
  props: {
    bookId: {
      type: String,
      required: true,
    },
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      this.loadBook(to.params.bookId)
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    canReadPages (): boolean {
      return this.$store.getters.mePageStreaming
    },
    canDownload (): boolean {
      return this.$store.getters.meFileDownload
    },
    thumbnailUrl (): string {
      return bookThumbnailUrl(this.bookId)
    },
    fileUrl (): string {
      return bookFileUrl(this.bookId)
    },
    format (): BookFormat {
      return getBookFormatFromMediaType(this.book.media.mediaType)
    },
    authorsByRole (): any {
      return groupAuthorsByRolePlural(this.book.metadata.authors)
    },
    isRead (): boolean {
      return getReadProgress(this.book) === ReadStatus.READ
    },
    isUnread (): boolean {
      return getReadProgress(this.book) === ReadStatus.UNREAD
    },
    isInProgress (): boolean {
      return getReadProgress(this.book) === ReadStatus.IN_PROGRESS
    },
    readProgressPercentage (): number {
      return getReadProgressPercentage(this.book)
    },
    readingDirection (): string {
      return this.$_.capitalize(this.book.metadata.readingDirection.replace(/_/g, ' '))
    },
    previousId (): string {
      return this.siblingPrevious?.id?.toString() || '0'
    },
    nextId (): string {
      return this.siblingNext?.id?.toString() || '0'
    },
  },
  methods: {
    libraryDeleted (event: EventLibraryDeleted) {
      if (event.id === this.book.libraryId) {
        this.$router.push({ name: 'home' })
      }
    },
    reloadBook (event: EventBookChanged) {
      if (event.id === this.bookId) this.loadBook(this.bookId)
    },
    async loadBook (bookId: string) {
      this.book = await this.$komgaBooks.getBook(bookId)
      this.series = await this.$komgaSeries.getOneSeries(this.book.seriesId)
      this.siblings = (await this.$komgaSeries.getBooks(this.book.seriesId, { unpaged: true } as PageRequest)).content

      if (this.$_.has(this.book, 'metadata.title')) {
        document.title = `Komga - ${getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)}`
      }

      try {
        this.siblingNext = await this.$komgaBooks.getBookSiblingNext(bookId)
      } catch (e) {
        this.siblingNext = {} as BookDto
      }
      try {
        this.siblingPrevious = await this.$komgaBooks.getBookSiblingPrevious(bookId)
      } catch (e) {
        this.siblingPrevious = {} as BookDto
      }
    },
    analyze () {
      this.$komgaBooks.analyzeBook(this.book)
    },
    refreshMetadata () {
      this.$komgaBooks.refreshMetadata(this.book)
    },
    editBook () {
      this.$store.dispatch('dialogUpdateBooks', this.book)
    },
  },
})
</script>

<style scoped>
</style>
