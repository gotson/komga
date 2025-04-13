<template>
  <div v-if="hasPages">
    <v-divider class="my-4" />
    <v-row>
      <v-col class="justify-center">
        <v-pagination 
          v-model="page"
          :total-visible="pageSize"
          :length="Math.ceil(pages.length / pageSize)"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col class="d-flex flex-wrap">
        <v-hover 
          v-for="page in visiblePages"
          :key="`${page.number}_${totalPages}`">
          <template  v-slot:default="{ hover }">
          <v-card
            class="my-2 mx-2"
            width="150">
            <v-img 
              :src="getThumbnailUrl(page)"
              aspect-ratio="0.7071" 
              :class="blur ? 'item-card blur' : 'item-card'">
              <v-fade-transition>
                <v-overlay
                  v-if="hover"
                  absolute
                  :opacity="hover ? 0.3 : 0"
                  :class="`${hover ? 'item-border-darken' : 'item-border-transparent'} overlay-full`">

                  <v-btn fab
                    x-large
                    color="accent"
                    style="position: absolute; top: 50%; left: 50%; margin-left: -36px; margin-top: -36px"
                    @click="goTo(page)">
                    <v-icon>mdi-book-open-page-variant</v-icon>
                  </v-btn>

                  <v-btn fab
                    small
                    :style="'position: absolute; top: 5px; ' + ($vuetify.rtl ? 'left' : 'right' ) +': 5px'"
                    @click="goTo(page, true)">
                    <v-icon>mdi-incognito</v-icon>
                  </v-btn>

                  <v-btn icon
                    v-if="isAdmin && canDelete(page)"
                    :style="'position: absolute; bottom: 5px; ' + ($vuetify.rtl ? 'left' : 'right' ) +': 5px'"
                    @click="promptDeletePage(page)">
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                
                </v-overlay>
              </v-fade-transition>
            </v-img>

            <v-card-subtitle v-line-clamp="2">{{ page.number }}
            </v-card-subtitle>
          </v-card>
          </template>
        </v-hover>
      </v-col>
    </v-row>

    <confirmation-dialog
      v-model="modalDeletePage"
      :title="$t('dialog.delete_page.dialog_title')"
      :body-html="$t('dialog.delete_page.warning_html', {number: pageToDelete.number})"
      :confirm-text="$t('dialog.delete_page.confirm_delete', {number: pageToDelete.number})"
      :button-confirm="$t('dialog.delete_page.button_confirm')"
      button-confirm-color="error"
      @confirm="deletePage"
    />

  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {bookPageThumbnailUrl} from '@/functions/urls'
import {PageDto} from '@/types/komga-books'
import {BOOK_CHANGED, ERROR} from '@/types/events'
import {BookSseDto} from '@/types/komga-sse'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'

export default Vue.extend({
  name: 'PagesBrowser',
  components: {
    ConfirmationDialog,
  },
  props: {
    bookId: {
      type: String,
    },
    readRouteName: {
      type: String,
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
      pages: [] as PageDto[],
      modalDeletePage: false,
      pageToDelete: {} as PageDto,
    }
  },
  async created() {
    this.loadPages(this.bookId)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
  },
  computed: {
    totalPages(): number {
      return this.pages.length
    },
    hasPages(): boolean {
      return this.totalPages > 0
    },
    currentPage(): number {
      if ((this.page - 1) * this.pageSize > this.totalPages)
        return Math.ceil(this.totalPages / this.pageSize)
      else
        return this.page
    },
    visiblePages(): PageDto[] {
      let start: number = (this.currentPage - 1) * this.pageSize
      let end: number = this.currentPage * this.pageSize
      return this.pages.slice(start, end)
    },
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    async loadPages(bookId: string) {
      this.pages = await this.$komgaBooks.getBookPages(bookId)
    },
    bookChanged(event: BookSseDto) {
      if (event.bookId === this.bookId) this.loadPages(this.bookId)
    },
    getThumbnailUrl(page: PageDto): string {
      return bookPageThumbnailUrl(this.bookId, page.number)
    },
    canDelete(page: PageDto): boolean {
      return (page.fileHash ?? '').trim().length !== 0
    },
    goTo(page: PageDto, incognito: boolean = false): void {
      this.$router.push(
        {
          name: this.readRouteName,
          params: {
            bookId: this.bookId,
          },
          query: {
            page: page.number.toString(),
            incognito: incognito.toString(),
          },
        })
    },
    promptDeletePage(page: PageDto): void {
      this.pageToDelete = page
      this.modalDeletePage = true
    },
    async deletePage() {
      try {
        await this.$komgaPageHashes.deleteSingleMatch(
          {
            hash: this.pageToDelete.fileHash ?? '',
            size: this.pageToDelete.sizeBytes,
            matchCount: 1,
          },
          {
            bookId: this.bookId,
            url: '',
            pageNumber: this.pageToDelete.number,
            fileName: this.pageToDelete.fileName,
            fileSize: this.pageToDelete.sizeBytes ?? 0,
            mediaType: this.pageToDelete.mediaType,
          },
        )
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },

})
</script>

<style scoped>
.blur > .v-image__image {
  filter: blur(5px);
}
</style>
