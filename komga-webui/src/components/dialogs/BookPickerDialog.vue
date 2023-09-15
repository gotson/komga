<template>
  <div>
    <v-dialog v-model="modal"
              max-width="600"
              scrollable
    >
      <v-card>
        <v-card-title>{{ $t('dialog.book_picker.title') }}</v-card-title>
        <v-btn icon absolute top right @click="dialogClose">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-divider/>

        <v-card-text style="height: 50%">
          <v-container fluid>
            <v-row align="center">
              <v-col>
                <v-text-field
                  v-model="filter"
                  autofocus
                  :label="$t('dialog.book_picker.filter')"
                />
              </v-col>
            </v-row>

            <v-divider/>

            <v-list v-if="filteredBooks.length > 0" three-line>
              <v-list-item-group color="primary" v-model="selectedItem">
                <v-list-item
                  v-for="(book, index) in filteredBooks"
                  :key="index"
                  @click="choose(book)"
                >
                  <v-img :src="bookThumbnailUrl(book.id)"
                         height="50"
                         max-width="35"
                         class="my-1 mx-3"
                         contain
                  />

                  <v-list-item-content>
                    <v-list-item-title>{{ book.metadata.title }}</v-list-item-title>
                    <v-list-item-subtitle>{{ book.metadata.number }}</v-list-item-subtitle>
                    <v-list-item-subtitle v-if="book.metadata.releaseDate">{{
                        new Intl.DateTimeFormat($i18n.locale, {
                          dateStyle: 'long',
                          timeZone: 'UTC'
                        }).format(new Date(book.metadata.releaseDate))
                      }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                </v-list-item>
              </v-list-item-group>
            </v-list>
          </v-container>
        </v-card-text>

      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {BookDto} from '@/types/komga-books'
import {bookThumbnailUrl} from '@/functions/urls'

export default Vue.extend({
  name: 'BookPickerDialog',
  data: () => {
    return {
      modal: false,
      selectedItem: -1,
      filter: '',
      bookThumbnailUrl,
    }
  },
  props: {
    value: Boolean,
    books: {
      type: Array as PropType<BookDto[]>,
      required: true,
    },
    book: {
      type: Object as PropType<BookDto>,
      required: false,
    },
  },
  watch: {
    value(val) {
      this.modal = val
      if (val) {
        this.clear()
      }
    },
    modal(val) {
      !val && this.dialogClose()
    },
    books(val) {
      this.selectBook(val, this.book)
    },
    book(val) {
      this.selectBook(this.books, val)
    },
  },
  computed: {
    filteredBooks(): BookDto[] {
      if (this.filter) {
        return this.books.filter((b) => b.metadata.number.toLowerCase().includes(this.filter.toLowerCase()) || b.metadata.title.toLowerCase().includes(this.filter.toLowerCase()) || b.metadata.releaseDate?.includes(this.filter))
      }
      return this.books
    },
  },
  methods: {
    clear() {
      this.filter = ''
    },
    choose(book: BookDto) {
      this.$emit('update:book', book)
      this.dialogClose()
    },
    dialogClose() {
      this.$emit('input', false)
    },
    selectBook(books: BookDto[], book?: BookDto) {
      this.selectedItem = this.$_.findIndex(books, (b) => b.id === book?.id)
    },
  },
})
</script>

<style scoped>

</style>
