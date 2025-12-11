<template>
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
          <v-tab class="justify-start">
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
          <v-tab class="justify-start" v-if="single">
            <v-icon left class="hidden-xs-only">mdi-link</v-icon>
            {{ $t('dialog.edit_books.tab_links') }}
          </v-tab>
          <v-tab class="justify-start" v-if="single">
            <v-icon left class="hidden-xs-only">mdi-image</v-icon>
            {{ $t('dialog.edit_books.tab_poster') }}
          </v-tab>
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-account-multiple</v-icon>
            {{ $t('dialog.edit_series.tab_sharing') }}
          </v-tab>

          <!--  Tab: General  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>

                <!--  Title  -->
                <v-row v-if="single">
                  <v-col cols="12">
                    <v-text-field v-model="form.book.title"
                                  :label="$t('dialog.edit_books.field_title')"
                                  filled
                                  dense
                                  :error-messages="requiredBookErrors('title')"
                                  @input="$v.form.book.title.$touch()"
                                  @blur="$v.form.book.title.$touch()"
                                  @change="form.book.titleLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.book.titleLock ? 'secondary' : ''"
                                @click="form.book.titleLock = !form.book.titleLock"
                        >
                          {{ form.book.titleLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <!--  Sort Title  -->
                <v-row v-if="single">
                  <v-col cols="12">
                    <v-text-field v-model="form.series.titleSort"
                                  :label="$t('dialog.edit_series.field_sort_title')"
                                  filled
                                  dense
                                  :error-messages="requiredSeriesErrors('titleSort')"
                                  @input="$v.form.series.titleSort.$touch()"
                                  @blur="$v.form.series.titleSort.$touch()"
                                  @change="form.series.titleSortLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.titleSortLock ? 'secondary' : ''"
                                @click="form.series.titleSortLock = !form.series.titleSortLock"
                        >
                          {{ form.series.titleSortLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <!--  Summary  -->
                <v-row v-if="single">
                  <v-col cols="12">
                    <v-textarea v-model="form.book.summary"
                                :label="$t('dialog.edit_books.field_summary')"
                                filled
                                dense
                                @input="$v.form.book.summary.$touch()"
                                @change="form.book.summaryLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.book.summaryLock ? 'secondary' : ''"
                                @click="form.book.summaryLock = !form.book.summaryLock"
                        >
                          {{ form.book.summaryLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-textarea>
                  </v-col>
                </v-row>

                <v-row>
                  <!--  Reading Direction  -->
                  <v-col cols="6">
                    <v-select v-model="form.series.readingDirection"
                              :items="readingDirections"
                              :label="$t('dialog.edit_series.field_reading_direction')"
                              clearable
                              filled
                              :placeholder="!single && mixed.readingDirection ? $t('dialog.edit_series.mixed') : ''"
                              @input="$v.form.series.readingDirection.$touch()"
                              @change="form.series.readingDirectionLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.readingDirectionLock ? 'secondary' : ''"
                                @click="form.series.readingDirectionLock = !form.series.readingDirectionLock"
                        >
                          {{ form.series.readingDirectionLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-select>
                  </v-col>

                  <!--  Language  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.series.language"
                                  :label="$t('dialog.edit_series.field_language')"
                                  filled
                                  dense
                                  :placeholder="!single && mixed.language ? $t('dialog.edit_series.mixed') : ''"
                                  :error-messages="languageErrors"
                                  :hint="$t('dialog.edit_series.field_language_hint')"
                                  @input="$v.form.series.language.$touch()"
                                  @change="form.series.languageLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.languageLock ? 'secondary' : ''"
                                @click="form.series.languageLock = !form.series.languageLock"
                        >
                          {{ form.series.languageLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <v-row>
                  <!--  Publisher  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.series.publisher"
                                  :label="$t('dialog.edit_series.field_publisher')"
                                  filled
                                  dense
                                  :placeholder="!single && mixed.publisher ? $t('dialog.edit_series.mixed') : ''"
                                  @input="$v.form.series.publisher.$touch()"
                                  @change="form.series.publisherLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.publisherLock ? 'secondary' : ''"
                                @click="form.series.publisherLock = !form.series.publisherLock"
                        >
                          {{ form.series.publisherLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>

                  <!--  Age Rating  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.series.ageRating"
                                  :label="$t('dialog.edit_series.field_age_rating')"
                                  clearable
                                  filled
                                  dense
                                  type="number"
                                  :placeholder="!single && mixed.ageRating ? $t('dialog.edit_series.mixed') : ''"
                                  :error-messages="ageRatingErrors"
                                  @input="$v.form.series.ageRating.$touch()"
                                  @blur="$v.form.series.ageRating.$touch()"
                                  @change="form.series.ageRatingLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.ageRatingLock ? 'secondary' : ''"
                                @click="form.series.ageRatingLock = !form.series.ageRatingLock"
                        >
                          {{ form.series.ageRatingLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <v-row v-if="single">
                  <!--  Release Date  -->
                  <v-col cols="6">
                    <v-menu
                      v-model="menuDate"
                      :close-on-content-click="false"
                      transition="scale-transition"
                      offset-y
                      min-width="auto"
                    >
                      <template v-slot:activator="{on, attrs}">
                        <v-text-field v-model="form.book.releaseDate"
                                      :label="$t('dialog.edit_books.field_release_date')"
                                      filled
                                      dense
                                      placeholder="YYYY-MM-DD"
                                      clearable
                                      :error-messages="releaseDateErrors"
                                      @blur="$v.form.book.releaseDate.$touch()"
                                      @change="form.book.releaseDateLock = true"
                                      v-bind="attrs"
                                      v-on="on"
                        >
                          <template v-slot:prepend>
                            <v-icon :color="form.book.releaseDateLock ? 'secondary' : ''"
                                    @click="form.book.releaseDateLock = !form.book.releaseDateLock"
                            >
                              {{ form.book.releaseDateLock ? 'mdi-lock' : 'mdi-lock-open' }}
                            </v-icon>
                          </template>
                        </v-text-field>
                      </template>
                      <v-date-picker v-model="form.book.releaseDate"
                                     :show-current="true"
                                     :show-adjacent-months="true"
                                     :locale="$i18n.locale"
                                     :first-day-of-week="$store.getters.getLocaleFirstDay()"
                                     @input="menuDate = false"
                                     @change="form.book.releaseDateLock = true"
                      />
                    </v-menu>
                  </v-col>

                  <!--  ISBN  -->
                  <v-col cols="6">
                    <v-text-field v-model.trim="form.book.isbn"
                                  :label="$t('dialog.edit_books.field_isbn')"
                                  filled
                                  dense
                                  placeholder="978-2-20-504375-4"
                                  clearable
                                  :error-messages="isbnErrors"
                                  @blur="$v.form.book.isbn.$touch()"
                                  @change="form.book.isbnLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.book.isbnLock ? 'secondary' : ''"
                                @click="form.book.isbnLock = !form.book.isbnLock"
                        >
                          {{ form.book.isbnLock ? 'mdi-lock' : 'mdi-lock-open' }}
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
                    <v-combobox v-model="form.book.authors[role.value]"
                                :items="authorSearchResultsFull"
                                :search-input.sync="authorSearch[i]"
                                @keydown.esc="authorSearch[i] = null"
                                @input="$v.form.book.authors.$touch()"
                                @change="form.book.authorsLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.book.authorsLock ? 'secondary' : ''"
                                @click="form.book.authorsLock = !form.book.authorsLock"
                        >
                          {{ form.book.authorsLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-combobox>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12" md="6">
                    <v-form
                      v-model="customRoleValid"
                      ref="customRoleForm"
                      @submit.prevent="addRole"
                    >
                      <v-text-field
                        label="Add custom role"
                        append-outer-icon="mdi-plus"
                        v-model.trim="customRole"
                        @click:append-outer="addRole"
                        :rules="customRoleRules"
                      ></v-text-field>
                    </v-form>
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

                <!-- Genres -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_series.field_genres') }}</span>
                    <v-combobox v-model="form.series.genres"
                                :items="genresAvailable"
                                @input="$v.form.series.genres.$touch()"
                                @change="form.series.genresLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.genresLock ? 'secondary' : ''"
                                @click="form.series.genresLock = !form.series.genresLock"
                        >
                          {{ form.series.genresLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-combobox>
                  </v-col>
                </v-row>

                <!-- Tags -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_books.field_tags') }}</span>
                    <v-combobox v-model="form.book.tags"
                                :items="tagsAvailable"
                                @input="$v.form.book.tags.$touch()"
                                @change="form.book.tagsLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.book.tagsLock ? 'secondary' : ''"
                                @click="form.book.tagsLock = !form.book.tagsLock"
                        >
                          {{ form.book.tagsLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-combobox>
                  </v-col>
                </v-row>
              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Links  -->
          <v-tab-item v-if="single">
            <v-card flat min-height="100">
              <v-container fluid>
                <!-- Links -->
                <v-form
                  v-model="linksValid"
                  ref="linksForm"
                >
                  <v-row
                    v-for="(link, i) in form.book.links"
                    :key="i"
                  >
                    <v-col cols="4" class="py-0">
                      <v-text-field v-model="form.book.links[i].label"
                                    :label="$t('dialog.edit_books.field_link_label')"
                                    filled
                                    dense
                                    :rules="[linksLabelRules]"
                                    @input="$v.form.book.links.$touch()"
                                    @blur="$v.form.book.links.$touch()"
                                    @change="form.book.linksLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.book.linksLock ? 'secondary' : ''"
                                  @click="form.book.linksLock = !form.book.linksLock"
                          >
                            {{ form.book.linksLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>

                    <v-col cols="8" class="py-0">
                      <v-text-field v-model="form.book.links[i].url"
                                    :label="$t('dialog.edit_books.field_link_url')"
                                    filled
                                    dense
                                    :rules="[linksUrlRules]"
                                    @input="$v.form.book.links.$touch()"
                                    @blur="$v.form.book.links.$touch()"
                                    @change="form.book.linksLock = true"
                      >
                        <template v-slot:append-outer>
                          <v-icon @click="form.book.links.splice(i, 1)">mdi-delete</v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>
                </v-form>

                <v-row>
                  <v-spacer/>
                  <v-col cols="auto">
                    <v-btn
                      elevation="2"
                      fab
                      small
                      bottom
                      right
                      color="primary"
                      @click="form.book.links.push({label:'', url:''})"
                    >
                      <v-icon>mdi-plus</v-icon>
                    </v-btn>
                  </v-col>
                </v-row>
              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Thumbnails  -->
          <v-tab-item v-if="single">
            <v-card flat>
              <v-container fluid>
                <!-- Upload -->
                <v-row>
                  <v-col class="pa-1">
                    <drop-zone ref="thumbnailsUpload" @on-input-change="addThumbnail" class="pa-8"/>
                  </v-col>
                </v-row>

                <!-- Gallery -->
                <v-row>
                  <v-col
                    cols="6" sm="4" lg="3" class="pa-1"
                    v-for="(item, index) in [...poster.uploadQueue, ...poster.bookThumbnails]"
                    :key="index"
                  >
                    <thumbnail-card
                      :item="item"
                      :selected="isThumbnailSelected(item)"
                      :toBeDeleted="isThumbnailToBeDeleted(item)"
                      @on-select-thumbnail="selectThumbnail"
                      @on-delete-thumbnail="deleteThumbnail"
                    />
                  </v-col>
                </v-row>

              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Sharing  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>
                <v-alert v-if="!single"
                         type="warning"
                         outlined
                         dense
                >{{ $t('dialog.edit_books.tags_notice_multiple_edit') }}
                </v-alert>

                <!-- Sharing Labels -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_series.field_labels') }}</span>
                    <v-combobox v-model="form.series.sharingLabels"
                                :items="sharingLabelsAvailable"
                                @input="$v.form.series.sharingLabels.$touch()"
                                @change="form.series.sharingLabelsLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.series.sharingLabelsLock ? 'secondary' : ''"
                                @click="form.series.sharingLabelsLock = !form.series.sharingLabelsLock"
                        >
                          {{ form.series.sharingLabelsLock ? 'mdi-lock' : 'mdi-lock-open' }}
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
          <v-btn color="primary" @click="dialogConfirm">{{ $t('dialog.edit_books.button_confirm') }}</v-btn>
        </v-card-actions>
      </v-card>
    </form>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {helpers, minValue, requiredIf} from 'vuelidate/lib/validators'
import {ReadingDirection} from '@/types/enum-books'
import {Oneshot, SeriesDto} from '@/types/komga-series'
import {ERROR, ErrorEvent} from '@/types/events'
import DropZone from '@/components/DropZone.vue'
import ThumbnailCard from '@/components/ThumbnailCard.vue'
import {BookDto, BookThumbnailDto} from '@/types/komga-books'
import {isMatch} from 'date-fns'
import IsbnVerify from '@w0s/isbn-verify'
import {debounce} from 'lodash'
import {authorRoles} from '@/types/author-roles'
import {groupAuthorsByRole} from '@/functions/authors'

const tags = require('language-tags')

const validLanguage = (value: string) => !helpers.req(value) || tags.check(value)
const validDate = (value: string) => !helpers.req(value) || isMatch(value, 'yyyy-MM-dd')
const validIsbn = (value: string) => !helpers.req(value) || new IsbnVerify(value).isIsbn13({check_digit: true})

export default Vue.extend({
  name: 'EditOneshotDialog',
  components: {ThumbnailCard, DropZone},
  data: () => {
    return {
      modal: false,
      tab: 0,
      menuDate: false,
      customRole: '',
      customRoles: [] as string[],
      customRoleValid: false,
      linksValid: false,
      form: {
        series: {
          titleSort: '',
          titleSortLock: false,
          readingDirection: '',
          readingDirectionLock: false,
          publisher: '',
          publisherLock: false,
          ageRating: undefined as number | undefined,
          ageRatingLock: false,
          language: '',
          languageLock: false,
          genres: [],
          genresLock: false,
          sharingLabels: [],
          sharingLabelsLock: false,
        },
        book: {
          title: '',
          titleLock: false,
          summary: '',
          summaryLock: false,
          releaseDate: '',
          releaseDateLock: false,
          authors: {},
          authorsLock: false,
          tags: [],
          tagsLock: false,
          isbn: '',
          isbnLock: false,
          links: [],
          linksLock: false,
        },
      },
      mixed: {
        readingDirection: false,
        publisher: false,
        ageRating: false,
        language: false,
      },
      poster: {
        selectedThumbnail: '',
        uploadQueue: [] as File[],
        deleteQueue: [] as BookThumbnailDto[],
        bookThumbnails: [] as BookThumbnailDto[],
      },
      authorSearch: [],
      authorSearchResults: [] as string[],
      genresAvailable: [] as string[],
      tagsAvailable: [] as string[],
      sharingLabelsAvailable: [] as string[],
    }
  },
  props: {
    value: Boolean,
    oneshots: {
      type: [Object as () => Oneshot, Array as () => Oneshot[]],
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      if (val) {
        this.getThumbnails(this.books)
        this.loadAvailableTags()
        this.loadAvailableGenres()
        this.loadAvailableSharingLabels()
      } else {
        this.dialogCancel()
      }
    },
    oneshots: {
      immediate: true,
      handler(val) {
        this.dialogReset(val)
      },
    },
    authorSearch: {
      deep: true,
      handler: debounce(async function (this: any, val: []) {
        const index = val.findIndex(x => x !== null)
        this.authorSearchResults = await this.$komgaReferential.getAuthorsNames(val[index])
      }, 500),
    },
  },
  validations: {
    form: {
      series: {
        titleSort: {
          required: requiredIf(function (this: any, model: any) {
            return this.single
          }),
        },
        language: {
          validLanguage: validLanguage,
        },
        genres: {},
        sharingLabels: {},
        ageRating: {minValue: minValue(0)},
        readingDirection: {},
        publisher: {},
      },
      book: {
        title: {
          required: requiredIf(function (this: any, model: any) {
            return this.single
          }),
        },
        summary: {},
        tags: {},
        releaseDate: {validDate},
        links: {},
        authors: {},
        isbn: {validIsbn},
      },
    },
  },
  computed: {
    series(): SeriesDto | SeriesDto[] {
      return this.single ? (this.oneshots as Oneshot).series : (this.oneshots as Oneshot[]).map(x => x.series)
    },
    books(): BookDto | BookDto[] {
      return this.single ? (this.oneshots as Oneshot).book : (this.oneshots as Oneshot[]).map(x => x.book)
    },
    single(): boolean {
      return !Array.isArray(this.oneshots)
    },
    authorRoles(): NameValue[] {
      let remoteRoles = [] as string[]
      if (Array.isArray(this.books))
        remoteRoles = this.books.flatMap(b => b.metadata.authors).map(a => a.role)
      else if (this.books?.metadata?.authors)
        remoteRoles = this.books.metadata.authors.map(a => a.role)
      const allRoles = this.$_.uniq([...authorRoles, ...remoteRoles, ...this.customRoles])
      return allRoles.map((role: string) => ({
        name: this.$te(`author_roles.${role}`) ? this.$t(`author_roles.${role}`).toString() : role,
        value: role,
      }))
    },
    authorSearchResultsFull(): string[] {
      // merge local values with server search, so that already input value is available
      const local = (this.$_.values(this.form.book.authors).flat()) as unknown as string[]
      // eslint-disable-next-line vue/no-side-effects-in-computed-properties
      return this.$_.sortBy(this.$_.union(local, this.authorSearchResults), x => x.toLowerCase())
    },
    readingDirections(): any[] {
      return Object.keys(ReadingDirection).map(x => (
        {
          text: this.$t(`enums.reading_direction.${x}`),
          value: x,
        }),
      )
    },
    ageRatingErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.series?.ageRating?.$dirty) return errors
      !this.$v?.form?.series?.ageRating?.minValue && errors.push(this.$t('dialog.edit_series.field_age_rating_error').toString())
      return errors
    },
    languageErrors(): string[] {
      if (!this.$v.form?.series?.language?.$dirty) return []
      if (!this.$v?.form?.series?.language?.validLanguage) return tags(this.form.series.language).errors().map((x: any) => x.message)
      return []
    },
    dialogTitle(): string {
      return this.single
        ? this.$t('dialog.edit_books.dialog_title_single', {book: this.$_.get(this.books, 'metadata.title')}).toString()
        : this.$tc('dialog.edit_books.dialog_title_multiple', (this.books as BookDto[]).length)
    },
    releaseDateErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.book?.releaseDate?.$dirty) return errors
      !this.$v?.form?.book?.releaseDate?.validDate && errors.push(this.$t('dialog.edit_books.field_release_date_error').toString())
      return errors
    },
    isbnErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.book?.isbn?.$dirty) return errors
      !this.$v?.form?.book?.isbn?.validIsbn && errors.push(this.$t('dialog.edit_books.field_isbn_error').toString())
      return errors
    },
    customRoleRules(): any[] {
      if (this.customRole === '') return [this.$t('common.required').toString()]
      if (this.authorRoles.map(n => n.value.toLowerCase()).includes(this.customRole?.toLowerCase())) return [this.$t('dialog.edit_books.add_author_role_error_duplicate').toString()]
      return [true]
    },
  },
  methods: {
    linksLabelRules(label: string): boolean | string {
      if (!!this.$_.trim(label)) return true
      return this.$t('common.required').toString()
    },
    linksUrlRules(value: string): boolean | string {
      let url
      try {
        url = new URL(value)
      } catch (_) {
        return this.$t('dialog.edit_books.field_link_url_error_url').toString()
      }
      if (url.protocol === 'http:' || url.protocol === 'https:') return true
      return this.$t('dialog.edit_books.field_link_url_error_protocol').toString()
    },
    addRole() {
      if ((this.$refs.customRoleForm as any).validate()) {
        this.customRoles.push(this.customRole.toLowerCase());
        (this.$refs.customRoleForm as any).reset()
      }
    },
    async loadAvailableTags() {
      this.tagsAvailable = await this.$komgaReferential.getTags()
    },
    async loadAvailableGenres() {
      this.genresAvailable = await this.$komgaReferential.getGenres()
    },
    async loadAvailableSharingLabels() {
      this.sharingLabelsAvailable = await this.$komgaReferential.getSharingLabels()
    },
    requiredBookErrors(fieldName: string): string[] {
      const errors = [] as string[]
      const formField = this.$v.form.book!![fieldName] as any
      if (!formField.$dirty) return errors
      !formField.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    requiredSeriesErrors(fieldName: string): string[] {
      const errors = [] as string[]
      const formField = this.$v.form.series!![fieldName] as any
      if (!formField.$dirty) return errors
      !formField.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    dialogReset(oneshots: Oneshot | Oneshot[]) {
      this.tab = 0;
      (this.$refs.customRoleForm as any)?.reset();
      (this.$refs.linksForm as any)?.resetValidation()
      this.customRoles = []
      this.$v.$reset()
      if (Array.isArray(oneshots) && oneshots.length === 0) return
      else if (this.$_.isEmpty(oneshots)) return
      if (Array.isArray(oneshots) && oneshots.length > 0) {
        const readingDirection = this.$_.uniq(oneshots.map(x => x.series.metadata.readingDirection))
        this.form.series.readingDirection = readingDirection.length > 1 ? '' : readingDirection[0]
        this.mixed.readingDirection = readingDirection.length > 1

        const readingDirectionLock = this.$_.uniq(oneshots.map(x => x.series.metadata.readingDirectionLock))
        this.form.series.readingDirectionLock = readingDirectionLock.length > 1 ? false : readingDirectionLock[0]

        const ageRating = this.$_.uniq(oneshots.map(x => x.series.metadata.ageRating))
        this.form.series.ageRating = ageRating.length > 1 ? undefined : ageRating[0]
        this.mixed.ageRating = ageRating.length > 1

        const ageRatingLock = this.$_.uniq(oneshots.map(x => x.series.metadata.ageRatingLock))
        this.form.series.ageRatingLock = ageRatingLock.length > 1 ? false : ageRatingLock[0]

        const publisher = this.$_.uniq(oneshots.map(x => x.series.metadata.publisher))
        this.form.series.publisher = publisher.length > 1 ? '' : publisher[0]
        this.mixed.publisher = publisher.length > 1

        const publisherLock = this.$_.uniq(oneshots.map(x => x.series.metadata.publisherLock))
        this.form.series.publisherLock = publisherLock.length > 1 ? false : publisherLock[0]

        const language = this.$_.uniq(oneshots.map(x => x.series.metadata.language))
        this.form.series.language = language.length > 1 ? '' : language[0]
        this.mixed.language = language.length > 1

        const languageLock = this.$_.uniq(oneshots.map(x => x.series.metadata.languageLock))
        this.form.series.languageLock = languageLock.length > 1 ? false : languageLock[0]

        this.form.series.genres = []

        const genresLock = this.$_.uniq(oneshots.map(x => x.series.metadata.genresLock))
        this.form.series.genresLock = genresLock.length > 1 ? false : genresLock[0]

        this.form.book.tags = []

        const tagsLock = this.$_.uniq(oneshots.map(x => x.book.metadata.tagsLock))
        this.form.book.tagsLock = tagsLock.length > 1 ? false : tagsLock[0]

        this.form.series.sharingLabels = []

        const sharingLabelsLock = this.$_.uniq(oneshots.map(x => x.series.metadata.sharingLabelsLock))
        this.form.series.sharingLabelsLock = sharingLabelsLock.length > 1 ? false : sharingLabelsLock[0]

        this.form.book.links = []
        this.form.book.authors = {}

        const authorsLock = this.$_.uniq(oneshots.map(x => x.book.metadata.authorsLock))
        this.form.book.authorsLock = authorsLock.length > 1 ? false : authorsLock[0]
      } else {
        this.form.series.genres = []
        this.form.series.sharingLabels = []
        this.form.book.tags = []
        this.form.book.links = []
        const oneshot = oneshots as Oneshot
        this.$_.merge(this.form.series, oneshot.series.metadata)
        this.$_.merge(this.form.book, oneshot.book.metadata)
        this.form.book.authors = groupAuthorsByRole(oneshot.book.metadata.authors)
        this.poster.selectedThumbnail = ''
        this.poster.deleteQueue = []
        this.poster.uploadQueue = []
        this.poster.bookThumbnails = []
      }
    },
    dialogCancel() {
      this.$emit('input', false)
      this.dialogReset(this.oneshots)
    },
    async dialogConfirm() {
      if (await this.editOneShots()) {
        this.$emit('input', false)
      }
    },
    validateForm(): any {
      if (!this.$v.$invalid
        && (!this.single || !this.$refs.linksForm || (this.$refs.linksForm as any).validate())
      ) {
        const metadataSeries = {
          readingDirectionLock: this.form.series.readingDirectionLock,
          ageRatingLock: this.form.series.ageRatingLock,
          publisherLock: this.form.series.publisherLock,
          languageLock: this.form.series.languageLock,
          genresLock: this.form.series.genresLock,
          sharingLabelsLock: this.form.series.sharingLabelsLock,
        }
        const metadataBook = {
          authorsLock: this.form.book.authorsLock,
          tagsLock: this.form.book.tagsLock,
        }

        if (this.$v.form?.book?.authors?.$dirty) {
          this.$_.merge(metadataBook, {
            authors: this.$_.keys(this.form.book.authors).flatMap((role: string) =>
              this.$_.get(this.form.book.authors, role).map((name: string) => ({name: name, role: role})),
            ),
          })
        }

        if (this.$v.form?.book?.tags?.$dirty) {
          this.$_.merge(metadataBook, {tags: this.form.book.tags})
        }

        if (this.$v.form?.series?.readingDirection?.$dirty) {
          this.$_.merge(metadataSeries, {readingDirection: this.form.series.readingDirection ? this.form.series.readingDirection : null})
        }

        if (this.$v.form?.series?.ageRating?.$dirty) {
          this.$_.merge(metadataSeries, {ageRating: this.form.series.ageRating})
        }

        if (this.$v.form?.series?.publisher?.$dirty) {
          this.$_.merge(metadataSeries, {publisher: this.form.series.publisher})
        }

        if (this.$v.form?.series?.genres?.$dirty) {
          this.$_.merge(metadataSeries, {genres: this.form.series.genres})
        }

        if (this.$v.form?.series?.language?.$dirty) {
          this.$_.merge(metadataSeries, {language: this.form.series.language})
        }

        if (this.$v.form?.series?.sharingLabels?.$dirty) {
          this.$_.merge(metadataSeries, {sharingLabels: this.form.series.sharingLabels})
        }

        if (this.single) {
          this.$_.merge(metadataSeries, {
            titleLock: this.form.book.titleLock,
            titleSortLock: this.form.series.titleSortLock,
            summaryLock: this.form.book.summaryLock,
          })

          this.$_.merge(metadataBook, {
            titleLock: this.form.book.titleLock,
            summaryLock: this.form.book.summaryLock,
            releaseDateLock: this.form.book.releaseDateLock,
            isbnLock: this.form.book.isbnLock,
            linksLock: this.form.book.linksLock,
          })

          if (this.$v.form?.series?.titleSort?.$dirty) {
            this.$_.merge(metadataSeries, {titleSort: this.form.series.titleSort})
          }

          if (this.$v.form?.book?.title?.$dirty) {
            this.$_.merge(metadataBook, {title: this.form.book.title})
            this.$_.merge(metadataSeries, {title: this.form.book.title})
          }

          if (this.$v.form?.book?.summary?.$dirty) {
            this.$_.merge(metadataBook, {summary: this.form.book.summary})
            this.$_.merge(metadataSeries, {summary: this.form.book.summary})
          }

          if (this.$v.form?.book?.releaseDate?.$dirty) {
            this.$_.merge(metadataBook, {releaseDate: this.form.book.releaseDate ? this.form.book.releaseDate : null})
          }

          if (this.$v.form?.book?.isbn?.$dirty) {
            this.$_.merge(metadataBook, {isbn: this.form.book.isbn})
          }

          if (this.$v.form?.book?.links?.$dirty || this.form.book.links.length != (this.books as BookDto).metadata.links?.length) {
            this.$_.merge(metadataBook, {links: this.form.book.links})
          }
        }

        return {series: metadataSeries, book: metadataBook}
      }
      return null
    },
    async editOneShots(): Promise<boolean> {
      if (this.single && this.poster.uploadQueue.length > 0) {
        const book = this.books as BookDto
        let hadErrors = false
        for (const file of this.poster.uploadQueue.slice()) {
          try {
            await this.$komgaBooks.uploadThumbnail(book.id, file, file.name === this.poster.selectedThumbnail)
            this.deleteThumbnail(file)
          } catch (e) {
            this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
            hadErrors = true
          }
        }
        if (hadErrors) {
          await this.getThumbnails(book)
          return false
        }
      }

      if (this.single && this.poster.selectedThumbnail !== '') {
        const id = this.poster.selectedThumbnail
        const book = this.books as BookDto
        if (this.poster.bookThumbnails.find(value => value.id === id)) {
          await this.$komgaBooks.markThumbnailAsSelected(book.id, id)
        }
      }

      if (this.single && this.poster.deleteQueue.length > 0) {
        this.poster.deleteQueue.forEach(toDelete => this.$komgaBooks.deleteThumbnail(toDelete.bookId, toDelete.id))
      }

      const metadata = this.validateForm()
      if (metadata) {
        const toUpdate = (this.single ? [this.oneshots] : this.oneshots) as Oneshot[]
        for (const o of toUpdate) {
          try {
            await this.$komgaSeries.updateMetadata(o.series.id, metadata.series)
            await this.$komgaBooks.updateMetadata(o.book.id, metadata.book)
          } catch (e) {
            this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
          }
        }
        return true
      } else return false
    },
    addThumbnail(files: File[]) {
      let hasSelected = false
      for (const file of files) {
        if (!this.poster.uploadQueue.find(value => value.name === file.name)) {
          this.poster.uploadQueue.push(file)
          if (!hasSelected) {
            this.selectThumbnail(file)
            hasSelected = true
          }
        }
      }

      (this.$refs.thumbnailsUpload as any).reset()
    },
    async getThumbnails(book: BookDto | BookDto[]) {
      if (Array.isArray(book)) return

      const thumbnails = await this.$komgaBooks.getThumbnails(book.id)

      this.selectThumbnail(thumbnails.find(x => x.selected))

      this.poster.bookThumbnails = thumbnails
    },
    isThumbnailSelected(item: File | BookThumbnailDto): boolean {
      return item instanceof File ? item.name === this.poster.selectedThumbnail : item.id === this.poster.selectedThumbnail
    },
    selectThumbnail(item: File | BookThumbnailDto | undefined) {
      if (!item) {
        return
      } else if (item instanceof File) {
        this.poster.selectedThumbnail = item.name
      } else {
        const index = this.poster.deleteQueue.indexOf(item, 0)
        if (index > -1) this.poster.deleteQueue.splice(index, 1)

        this.poster.selectedThumbnail = item.id
      }
    },
    isThumbnailToBeDeleted(item: File | BookThumbnailDto) {
      if (item instanceof File) {
        return false
      } else {
        return this.poster.deleteQueue.includes(item)
      }
    },
    deleteThumbnail(item: File | BookThumbnailDto) {
      if (item instanceof File) {
        const index = this.poster.uploadQueue.indexOf(item, 0)
        if (index > -1) {
          this.poster.uploadQueue.splice(index, 1)
        }
        if (item.name === this.poster.selectedThumbnail) {
          this.poster.selectedThumbnail = ''
        }
      } else {
        // if thumbnail was marked for deletion, unmark it
        if (this.isThumbnailToBeDeleted(item)) {
          const index = this.poster.deleteQueue.indexOf(item, 0)
          if (index > -1) {
            this.poster.deleteQueue.splice(index, 1)
          }
        } else {
          this.poster.deleteQueue.push(item)
          if (item.id === this.poster.selectedThumbnail) this.poster.selectedThumbnail = ''
        }
      }
    },
  },
})
</script>

<style lang="sass" scoped>
@import 'src/styles/tabbed-dialog'
</style>
