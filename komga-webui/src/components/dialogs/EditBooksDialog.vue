<template>
  <div>
    <v-dialog v-model="modal"
              :fullscreen="$vuetify.breakpoint.xsOnly"
              max-width="800"
              @keydown.esc="dialogCancel"
    >
      <form novalidate>
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

          <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
            <v-tab class="justify-start" v-if="single">
              <v-icon left class="hidden-xs-only">mdi-format-align-center</v-icon>
              {{ $t('dialog.edit_books.tab_general') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-account-multiple</v-icon>
              {{ $t('dialog.edit_books.tab_authors') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-tag-multiple</v-icon>
              {{ $t('dialog.edit_books.tab_tags') }}
            </v-tab>

            <!--  Tab: General  -->
            <v-tab-item v-if="single">
              <v-card flat>
                <v-container fluid>

                  <!--  Title  -->
                  <v-row v-if="single">
                    <v-col cols="12">
                      <v-text-field v-model="form.title"
                                    :label="$t('dialog.edit_books.field_title')"
                                    filled
                                    dense
                                    :error-messages="requiredErrors('title')"
                                    @input="$v.form.title.$touch()"
                                    @blur="$v.form.title.$touch()"
                                    @change="form.titleLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.titleLock ? 'secondary' : ''"
                                  @click="form.titleLock = !form.titleLock"
                          >
                            {{ form.titleLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>

                  <!--  Number  -->
                  <v-row v-if="single">
                    <v-col cols="6">
                      <v-text-field v-model="form.number"
                                    :label="$t('dialog.edit_books.field_number')"
                                    filled
                                    dense
                                    :error-messages="requiredErrors('number')"
                                    @input="$v.form.number.$touch()"
                                    @blur="$v.form.number.$touch()"
                                    @change="form.numberLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.numberLock ? 'secondary' : ''"
                                  @click="form.numberLock = !form.numberLock"
                          >
                            {{ form.numberLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>

                    <!--  Sort Number  -->
                    <v-col cols="6">
                      <v-text-field v-model="form.numberSort"
                                    type="number"
                                    step="0.1"
                                    :label="$t('dialog.edit_books.field_number_sort')"
                                    filled
                                    dense
                                    :hint="$t('dialog.edit_books.field_number_sort_hint')"
                                    persistent-hint
                                    :error-messages="requiredErrors('numberSort')"
                                    @input="$v.form.numberSort.$touch()"
                                    @blur="$v.form.numberSort.$touch()"
                                    @change="form.numberSortLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.numberSortLock ? 'secondary' : ''"
                                  @click="form.numberSortLock = !form.numberSortLock"
                          >
                            {{ form.numberSortLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>

                  <!--  Summary  -->
                  <v-row v-if="single">
                    <v-col cols="12">
                      <v-textarea v-model="form.summary"
                                  :label="$t('dialog.edit_books.field_summary')"
                                  filled
                                  dense
                                  @input="$v.form.summary.$touch()"
                                  @change="form.summaryLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.summaryLock ? 'secondary' : ''"
                                  @click="form.summaryLock = !form.summaryLock"
                          >
                            {{ form.summaryLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-textarea>
                    </v-col>
                  </v-row>

                  <v-row v-if="single">
                    <!--  Release Date  -->
                    <v-col cols="6">
                      <v-text-field v-model="form.releaseDate"
                                    :label="$t('dialog.edit_books.field_release_date')"
                                    filled
                                    dense
                                    placeholder="YYYY-MM-DD"
                                    clearable
                                    :error-messages="releaseDateErrors"
                                    @blur="$v.form.releaseDate.$touch()"
                                    @change="form.releaseDateLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.releaseDateLock ? 'secondary' : ''"
                                  @click="form.releaseDateLock = !form.releaseDateLock"
                          >
                            {{ form.releaseDateLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>

                    <!--  ISBN  -->
                    <v-col cols="6">
                      <v-text-field v-model.trim="form.isbn"
                                    :label="$t('dialog.edit_books.field_isbn')"
                                    filled
                                    dense
                                    placeholder="978-2-20-504375-4"
                                    clearable
                                    :error-messages="isbnErrors"
                                    @blur="$v.form.isbn.$touch()"
                                    @change="form.isbnLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.isbnLock ? 'secondary' : ''"
                                  @click="form.isbnLock = !form.isbnLock"
                          >
                            {{ form.isbnLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>

                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Authors  -->
            <v-tab-item>
              <v-card flat>
                <v-container fluid>
                  <v-alert v-if="!single"
                           type="warning"
                           outlined
                           dense
                  >{{ $t('dialog.edit_books.authors_notice_multiple_edit') }}
                  </v-alert>
                  <v-row v-for="(role, i) in authorRoles"
                         :key="i"
                  >
                    <v-col cols="12">
                      <span class="text-body-2">{{ $_.capitalize(role.name) }}</span>
                      <v-combobox v-model="form.authors[role.value]"
                                  :items="authorSearchResultsFull"
                                  :search-input.sync="authorSearch[i]"
                                  @keydown.esc="authorSearch[i] = null"
                                  @input="$v.form.authors.$touch()"
                                  @change="form.authorsLock = true"
                                  hide-selected
                                  chips
                                  deletable-chips
                                  multiple
                                  filled
                                  dense
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.authorsLock ? 'secondary' : ''"
                                  @click="form.authorsLock = !form.authorsLock"
                          >
                            {{ form.authorsLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-combobox>
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Tags  -->
            <v-tab-item>
              <v-card flat>
                <v-container fluid>
                  <v-alert v-if="!single"
                           type="warning"
                           outlined
                           dense
                  >{{ $t('dialog.edit_books.tags_notice_multiple_edit') }}
                  </v-alert>

                  <!-- Tags -->
                  <v-row>
                    <v-col cols="12">
                      <span class="text-body-2">{{ $t('dialog.edit_books.field_tags') }}</span>
                      <v-combobox v-model="form.tags"
                                  :items="tagsAvailable"
                                  @input="$v.form.tags.$touch()"
                                  @change="form.tagsLock = true"
                                  hide-selected
                                  chips
                                  deletable-chips
                                  multiple
                                  filled
                                  dense
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.tagsLock ? 'secondary' : ''"
                                  @click="form.tagsLock = !form.tagsLock"
                          >
                            {{ form.tagsLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-combobox>
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>

          </v-tabs>

          <v-card-actions class="hidden-xs-only">
            <v-spacer/>
            <v-btn text @click="dialogCancel">{{ $t('dialog.edit_books.button_cancel') }}</v-btn>
            <v-btn text class="primary--text" @click="dialogConfirm">{{ $t('dialog.edit_books.button_confirm') }}</v-btn>
          </v-card-actions>
        </v-card>
      </form>
    </v-dialog>

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >{{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import {groupAuthorsByRole} from '@/functions/authors'
import {authorRoles} from '@/types/author-roles'
import Vue from 'vue'
import {helpers, requiredIf} from 'vuelidate/lib/validators'
import {BookDto} from '@/types/komga-books'
import IsbnVerify from '@saekitominaga/isbn-verify'
import {isMatch} from 'date-fns'

const validDate = (value: string) => !helpers.req(value) || isMatch(value, 'yyyy-MM-dd')
const validIsbn = (value: string) => !helpers.req(value) || new IsbnVerify(value).isIsbn13({ check_digit: true })

export default Vue.extend({
  name: 'EditBooksDialog',
  data: () => {
    return {
      modal: false,
      snackbar: false,
      snackText: '',
      tab: 0,
      form: {
        title: '',
        titleLock: false,
        summary: '',
        summaryLock: false,
        number: '',
        numberLock: false,
        numberSort: 0,
        numberSortLock: false,
        releaseDate: '',
        releaseDateLock: false,
        authors: {},
        authorsLock: false,
        tags: [] as string[],
        tagsLock: false,
        isbn: '',
        isbnLock: false,
      },
      authorSearch: [],
      authorSearchResults: [] as string[],
      tagsAvailable: [] as string[],
    }
  },
  props: {
    value: Boolean,
    books: {
      type: [Object as () => BookDto, Array as () => BookDto[]],
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
    authorSearch: {
      deep: true,
      async handler(val: []) {
        const index = val.findIndex(x => x !== null)
        this.authorSearchResults = await this.$komgaReferential.getAuthorsNames(val[index])
      },
    },
  },
  validations: {
    form: {
      title: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      number: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      numberSort: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      tags: {},
      releaseDate: {validDate},
      summary: {},
      authors: {},
      isbn: {validIsbn},
    },
  },
  async created() {
    this.tagsAvailable = await this.$komgaReferential.getTags()
  },
  computed: {
    authorRoles(): NameValue[] {
      return authorRoles.map((x: string) => ({
        name: this.$t(`author_roles.${x}`).toString(),
        value: x,
      }))
    },
    single(): boolean {
      return !Array.isArray(this.books)
    },
    authorSearchResultsFull(): string[] {
      // merge local values with server search, so that already input value is available
      const local = (this.$_.values(this.form.authors).flat()) as unknown as string[]
      return this.$_.sortBy(this.$_.union(local, this.authorSearchResults), x => x.toLowerCase())
    },
    dialogTitle (): string {
      return this.single
        ? this.$t('dialog.edit_books.dialog_title_single', {book: this.$_.get(this.books, 'metadata.title')}).toString()
        : this.$tc('dialog.edit_books.dialog_title_multiple', (this.books as BookDto[]).length)
    },
    releaseDateErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.releaseDate?.$dirty) return errors
      !this.$v?.form?.releaseDate?.validDate && errors.push(this.$t('dialog.edit_books.field_release_date_error').toString())
      return errors
    },
    isbnErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.isbn?.$dirty) return errors
      !this.$v?.form?.isbn?.validIsbn && errors.push(this.$t('dialog.edit_books.field_isbn_error').toString())
      return errors
    },
  },
  methods: {
    requiredErrors(fieldName: string): string[] {
      const errors = [] as string[]
      const formField = this.$v.form!![fieldName] as any
      if (!formField.$dirty) return errors
      !formField.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    dialogReset(books: BookDto | BookDto[]) {
      this.tab = 0
      this.$v.$reset()
      if (Array.isArray(books) && books.length === 0) return
      else if (this.$_.isEmpty(books)) return
      if (Array.isArray(books) && books.length > 0) {
        this.form.authors = {}

        const authorsLock = this.$_.uniq(books.map(x => x.metadata.authorsLock))
        this.form.authorsLock = authorsLock.length > 1 ? false : authorsLock[0]

        this.form.tags = []

        const tagsLock = this.$_.uniq(books.map(x => x.metadata.tagsLock))
        this.form.tagsLock = tagsLock.length > 1 ? false : tagsLock[0]
      } else {
        this.form.tags = []
        const book = books as BookDto
        this.$_.merge(this.form, book.metadata)
        this.form.authors = groupAuthorsByRole(book.metadata.authors)
      }
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
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    validateForm(): any {
      if (!this.$v.$invalid) {
        const metadata = {
          authorsLock: this.form.authorsLock,
          tagsLock: this.form.tagsLock,
        }

        if (this.$v.form?.authors?.$dirty) {
          this.$_.merge(metadata, {
            authors: this.$_.keys(this.form.authors).flatMap((role: string) =>
              this.$_.get(this.form.authors, role).map((name: string) => ({name: name, role: role})),
            ),
          })
        }

        if (this.$v.form?.tags?.$dirty) {
          this.$_.merge(metadata, {tags: this.form.tags})
        }

        if (this.single) {
          this.$_.merge(metadata, {
            titleLock: this.form.titleLock,
            numberLock: this.form.numberLock,
            numberSortLock: this.form.numberSortLock,
            summaryLock: this.form.summaryLock,
            releaseDateLock: this.form.releaseDateLock,
            isbnLock: this.form.isbnLock,
          })

          if (this.$v.form?.title?.$dirty) {
            this.$_.merge(metadata, {title: this.form.title})
          }

          if (this.$v.form?.number?.$dirty) {
            this.$_.merge(metadata, {number: this.form.number})
          }

          if (this.$v.form?.numberSort?.$dirty) {
            this.$_.merge(metadata, {numberSort: this.form.numberSort})
          }

          if (this.$v.form?.summary?.$dirty) {
            this.$_.merge(metadata, {summary: this.form.summary})
          }

          if (this.$v.form?.releaseDate?.$dirty) {
            this.$_.merge(metadata, {releaseDate: this.form.releaseDate ? this.form.releaseDate : null})
          }

          if (this.$v.form?.isbn?.$dirty) {
            this.$_.merge(metadata, {isbn: this.form.isbn})
          }
        }

        return metadata
      }
      return null
    },
    async editBooks(): Promise<boolean> {
      const metadata = this.validateForm()
      if (metadata) {
        const updated = [] as BookDto[]
        const toUpdate = (this.single ? [this.books] : this.books) as BookDto[]
        for (const b of toUpdate) {
          try {
            await this.$komgaBooks.updateMetadata(b.id, metadata)
          } catch (e) {
            this.showSnack(e.message)
          }
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
