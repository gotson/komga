<template>
  <v-dialog v-model="modal"
            :fullscreen="$vuetify.breakpoint.xsOnly"
            scrollable
            @keydown.esc="dialogCancel"
  >
    <v-form ref="form">
      <v-card>
        <v-toolbar class="hidden-sm-and-up">
          <v-btn icon @click="dialogCancel">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>{{ dialogTitle }}</v-toolbar-title>
          <v-spacer/>
          <v-toolbar-items>
            <v-btn text color="primary" @click="dialogConfirm">{{ $t('dialog.edit_books.button_confirm') }}</v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-card-title class="hidden-xs-only">
          <v-icon class="mx-4">mdi-pencil</v-icon>
          {{ dialogTitle }}
        </v-card-title>

        <v-card-subtitle>
          <v-container fluid>

            <v-row>
              <v-col
                v-for="(prop, i) in headerRow"
                :key="i"
                :cols="prop.cols ? prop.cols : undefined"
              >
                <v-btn icon @click="changeAllLock(prop.prop, lockStatus(prop.prop) !== 2)">
                  <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-icon v-on="on" :color="lockStatus(prop.prop) === 2 ? 'secondary' : ''">
                        {{ lockStatus(prop.prop) !== 0 ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                    <span>{{ lockStatus(prop.prop) !== 2 ? $t('common.lock_all') : $t('common.unlock_all') }}</span>
                  </v-tooltip>
                </v-btn>

                <span class="subtitle-1">{{ prop.label }}</span>
                <v-btn v-if="prop.prop === 'number'" icon @click="copyFromNumberSort">
                  <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-icon v-on="on">mdi-content-copy</v-icon>
                    </template>
                    <span>{{ $t('dialog.edit_books.copy_from', {field: $t('dialog.edit_books.field_number_sort')}) }}</span>
                  </v-tooltip>
                </v-btn>
                <span v-if="prop.prop === 'numberSort'">
                  <v-btn icon @click="numberSortDecrement">
                    <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-icon v-on="on">mdi-numeric-negative-1</v-icon>
                    </template>
                    <span>{{ $t('dialog.edit_books.number_sort_decrement') }}</span>
                  </v-tooltip>
                  </v-btn>
                  <v-btn icon @click="numberSortIncrement">
                    <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-icon v-on="on">mdi-numeric-positive-1</v-icon>
                    </template>
                    <span>{{ $t('dialog.edit_books.number_sort_increment') }}</span>
                  </v-tooltip>
                  </v-btn>
                </span>
              </v-col>
            </v-row>
          </v-container>
        </v-card-subtitle>

        <v-card-text>
          <v-container fluid>

            <div v-for="(book, i) in books"
                 :key="book.id"
                 class="pa-2"
            >
              <div class="subtitle-2 mb-2">{{ bookDisplayName(book) }}</div>

              <!--  Title  -->
              <v-row>
                <v-col>
                  <v-text-field v-model="form[book.id].title"
                                dense
                                validate-on-blur
                                :rules="[validateRequired]"
                                @change="form[book.id].titleLock = true"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="form[book.id].titleLock ? 'secondary' : ''"
                              @click="form[book.id].titleLock = !form[book.id].titleLock"
                      >
                        {{ form[book.id].titleLock ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                  </v-text-field>
                </v-col>

                <!--  Number  -->
                <v-col cols="2">
                  <v-text-field v-model="form[book.id].number"
                                dense
                                validate-on-blur
                                :rules="[validateRequired]"
                                @change="form[book.id].numberLock = true"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="form[book.id].numberLock ? 'secondary' : ''"
                              @click="form[book.id].numberLock = !form[book.id].numberLock"
                      >
                        {{ form[book.id].numberLock ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                  </v-text-field>
                </v-col>

                <!--  Sort Number  -->
                <v-col cols="2">
                  <v-text-field v-model.number="form[book.id].numberSort"
                                type="number"
                                step="0.1"
                                dense
                                validate-on-blur
                                :rules="[validateRequired]"
                                @change="form[book.id].numberSortLock = true"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="form[book.id].numberSortLock ? 'secondary' : ''"
                              @click="form[book.id].numberSortLock = !form[book.id].numberSortLock"
                      >
                        {{ form[book.id].numberSortLock ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                  </v-text-field>
                </v-col>

                <!--  Release Date  -->
                <v-col cols="2">
                  <v-text-field v-model="form[book.id].releaseDate"
                                dense
                                placeholder="YYYY-MM-DD"
                                clearable
                                validate-on-blur
                                :rules="[validateReleaseDate]"
                                @change="form[book.id].releaseDateLock = true"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="form[book.id].releaseDateLock ? 'secondary' : ''"
                              @click="form[book.id].releaseDateLock = !form[book.id].releaseDateLock"
                      >
                        {{ form[book.id].releaseDateLock ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                  </v-text-field>
                </v-col>

                <!--  ISBN  -->
                <v-col cols="2">
                  <v-text-field v-model.trim="form[book.id].isbn"
                                dense
                                placeholder="978-2-20-504375-4"
                                clearable
                                validate-on-blur
                                :rules="[validateIsbn]"
                                @change="form[book.id].isbnLock = true"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="form[book.id].isbnLock ? 'secondary' : ''"
                              @click="form[book.id].isbnLock = !form[book.id].isbnLock"
                      >
                        {{ form[book.id].isbnLock ? 'mdi-lock' : 'mdi-lock-open' }}
                      </v-icon>
                    </template>
                  </v-text-field>
                </v-col>
              </v-row>

              <v-divider v-if="i !== (books.length - 1)"/>
            </div>

          </v-container>
        </v-card-text>

        <v-card-actions class="hidden-xs-only">
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_books.button_cancel') }}</v-btn>
          <v-btn color="primary" @click="dialogConfirm">{{ $t('dialog.edit_books.button_confirm') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {BookDto} from '@/types/komga-books'
import IsbnVerify from '@saekitominaga/isbn-verify'
import {isMatch} from 'date-fns'
import {ERROR} from '@/types/events'

export default Vue.extend({
  name: 'BulkEditBooksDialog',
  data: () => {
    return {
      modal: false,
      form: {
        'sampleId': {
          title: '',
          titleLock: false,
          number: '',
          numberLock: false,
          numberSort: 0,
          numberSortLock: false,
          releaseDate: '',
          releaseDateLock: false,
          isbn: '',
          isbnLock: false,
        },
      } as any,
    }
  },
  props: {
    value: Boolean,
    books: {
      type: Array as () => BookDto[],
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      !val && this.dialogCancel()
    },
    books: {
      immediate: true,
      handler(val) {
        this.dialogReset(val)
      },
    },
  },
  computed: {
    dialogTitle(): string {
      return this.$tc('dialog.edit_books.dialog_title_multiple', this.books.length)
    },
    headerRow(): object[] {
      return [
        {
          prop: 'title',
          label: this.$t('dialog.edit_books.field_title'),
        },
        {
          prop: 'number',
          label: this.$t('dialog.edit_books.field_number'),
          cols: 2,
        }, {
          prop: 'numberSort',
          label: this.$t('dialog.edit_books.field_number_sort'),
          cols: 2,
        }, {
          prop: 'releaseDate',
          label: this.$t('dialog.edit_books.field_release_date'),
          cols: 2,
        }, {
          prop: 'isbn',
          label: this.$t('dialog.edit_books.field_isbn'),
          cols: 2,
        }]
    },
  },
  methods: {
    validateIsbn(isbn: string): string | boolean {
      return isbn && !new IsbnVerify(isbn).isIsbn13({check_digit: true}) ? this.$t('dialog.edit_books.field_isbn_error').toString() : true
    },
    validateRequired(value: any): string | boolean {
      return value || value === 0 ? true : this.$t('common.required').toString()
    },
    validateReleaseDate(date: string): string | boolean {
      return date && (!isMatch(date, 'yyyy-MM-dd') || date.length !== 10) ? this.$t('dialog.edit_books.field_release_date_error').toString() : true
    },
    bookDisplayName(book: BookDto): string {
      const parts = book.url.split('/')
      return parts[parts.length - 2] + '/' + parts[parts.length - 1]
    },
    lockStatus(prop: string): number {
      const propLock = `${prop}Lock`
      let count = 0
      for (const book of this.books) {
        if (this.form[book.id][propLock]) count++
      }
      if (count === 0) return 0
      if (count === this.books.length) return 2
      return 1
    },
    changeAllLock(prop: string, lock: boolean) {
      const propLock = `${prop}Lock`
      for (const book of this.books) {
        this.form[book.id][propLock] = lock
      }
    },
    numberSortDecrement() {
      for (const book of this.books) {
        this.form[book.id].numberSort = this.form[book.id].numberSort - 1
      }
    },
    numberSortIncrement() {
      for (const book of this.books) {
        this.form[book.id].numberSort = this.form[book.id].numberSort + 1
      }
    },
    copyFromNumberSort() {
      for (const book of this.books) {
        this.form[book.id].number = this.form[book.id].numberSort
      }
    },
    dialogReset(books: BookDto[]) {
      (this.$refs.form as any)?.resetValidation()
      this.form = books.reduce((accum, current) => {
        accum[current.id] = {
          title: current.metadata.title,
          titleLock: current.metadata.titleLock,
          number: current.metadata.number,
          numberLock: current.metadata.numberLock,
          numberSort: current.metadata.numberSort,
          numberSortLock: current.metadata.numberSortLock,
          releaseDate: current.metadata.releaseDate,
          releaseDateLock: current.metadata.releaseDateLock,
          isbn: current.metadata.isbn,
          isbnLock: current.metadata.isbnLock,
        }
        return accum
      }, {} as any)
    },
    dialogCancel() {
      this.$emit('input', false)
      this.dialogReset(this.books)
    },
    async dialogConfirm() {
      if (await this.editBooks()) {
        this.$emit('input', false)
      }
    },
    validateForm(): any {
      if ((this.$refs.form as any).validate()) {
        return this.form
      }
      return null
    },
    async editBooks(): Promise<boolean> {
      const metadata = this.validateForm()
      if (metadata) {
        try {
          await this.$komgaBooks.updateMetadataBatch(metadata)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
        return true
      } else return false
    },
  },
})
</script>

<style lang="sass" scoped>
@import '../../styles/tabbed-dialog'
</style>
