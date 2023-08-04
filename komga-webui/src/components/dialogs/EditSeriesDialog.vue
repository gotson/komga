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
            <v-btn text color="primary" @click="dialogConfirm">{{ $t('dialog.edit_series.button_confirm') }}</v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-card-title class="hidden-xs-only">
          <v-icon class="mx-4">mdi-pencil</v-icon>
          {{ dialogTitle }}
        </v-card-title>

        <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-format-align-center</v-icon>
            {{ $t('dialog.edit_series.tab_general') }}
          </v-tab>
          <v-tab class="justify-start" v-if="single">
            <v-icon left class="hidden-xs-only">mdi-format-title</v-icon>
            {{ $t('dialog.edit_series.tab_titles') }}
          </v-tab>
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-tag-multiple</v-icon>
            {{ $t('dialog.edit_series.tab_tags') }}
          </v-tab>
          <v-tab class="justify-start" v-if="single">
            <v-icon left class="hidden-xs-only">mdi-link</v-icon>
            {{ $t('dialog.edit_books.tab_links') }}
          </v-tab>
          <v-tab class="justify-start" v-if="single">
            <v-icon left class="hidden-xs-only">mdi-image</v-icon>
            {{ $t('dialog.edit_series.tab_poster') }}
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
                    <v-text-field v-model="form.title"
                                  :label="$t('dialog.edit_series.field_title')"
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

                <!--  Sort Title  -->
                <v-row v-if="single">
                  <v-col cols="12">
                    <v-text-field v-model="form.titleSort"
                                  :label="$t('dialog.edit_series.field_sort_title')"
                                  filled
                                  dense
                                  :error-messages="requiredErrors('titleSort')"
                                  @input="$v.form.titleSort.$touch()"
                                  @blur="$v.form.titleSort.$touch()"
                                  @change="form.titleSortLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.titleSortLock ? 'secondary' : ''"
                                @click="form.titleSortLock = !form.titleSortLock"
                        >
                          {{ form.titleSortLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <!--  Summary  -->
                <v-row v-if="single">
                  <v-col cols="12">
                    <v-textarea v-model="form.summary"
                                :label="$t('dialog.edit_series.field_summary')"
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

                <v-row>
                  <!--  Status  -->
                  <v-col cols="6">
                    <v-select :items="seriesStatus"
                              v-model="form.status"
                              :label="$t('dialog.edit_series.field_status')"
                              filled
                              :placeholder="!single && mixed.status ? $t('dialog.edit_series.mixed') : ''"
                              @input="$v.form.status.$touch()"
                              @change="form.statusLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.statusLock ? 'secondary' : ''"
                                @click="form.statusLock = !form.statusLock"
                        >
                          {{ form.statusLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-select>
                  </v-col>

                  <!--  Language  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.language"
                                  :label="$t('dialog.edit_series.field_language')"
                                  filled
                                  dense
                                  :placeholder="!single && mixed.language ? $t('dialog.edit_series.mixed') : ''"
                                  :error-messages="languageErrors"
                                  :hint="$t('dialog.edit_series.field_language_hint')"
                                  @input="$v.form.language.$touch()"
                                  @change="form.languageLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.languageLock ? 'secondary' : ''"
                                @click="form.languageLock = !form.languageLock"
                        >
                          {{ form.languageLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <!--  Reading Direction  -->
                <v-row>
                  <v-col cols="12">
                    <v-select v-model="form.readingDirection"
                              :items="readingDirections"
                              :label="$t('dialog.edit_series.field_reading_direction')"
                              clearable
                              filled
                              :placeholder="!single && mixed.readingDirection ? $t('dialog.edit_series.mixed') : ''"
                              @input="$v.form.readingDirection.$touch()"
                              @change="form.readingDirectionLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.readingDirectionLock ? 'secondary' : ''"
                                @click="form.readingDirectionLock = !form.readingDirectionLock"
                        >
                          {{ form.readingDirectionLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-select>
                  </v-col>
                </v-row>

                <v-row>
                  <!--  Publisher  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.publisher"
                                  :label="$t('dialog.edit_series.field_publisher')"
                                  filled
                                  dense
                                  :placeholder="!single && mixed.publisher ? $t('dialog.edit_series.mixed') : ''"
                                  @input="$v.form.publisher.$touch()"
                                  @change="form.publisherLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.publisherLock ? 'secondary' : ''"
                                @click="form.publisherLock = !form.publisherLock"
                        >
                          {{ form.publisherLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>

                  <!--  Age Rating  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.ageRating"
                                  :label="$t('dialog.edit_series.field_age_rating')"
                                  clearable
                                  filled
                                  dense
                                  type="number"
                                  :placeholder="!single && mixed.ageRating ? $t('dialog.edit_series.mixed') : ''"
                                  :error-messages="ageRatingErrors"
                                  @input="$v.form.ageRating.$touch()"
                                  @blur="$v.form.ageRating.$touch()"
                                  @change="form.ageRatingLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.ageRatingLock ? 'secondary' : ''"
                                @click="form.ageRatingLock = !form.ageRatingLock"
                        >
                          {{ form.ageRatingLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

                <v-row v-if="single">
                  <!--  Total book count  -->
                  <v-col cols="6">
                    <v-text-field v-model="form.totalBookCount"
                                  :label="$t('dialog.edit_series.field_total_book_count')"
                                  clearable
                                  filled
                                  dense
                                  type="number"
                                  :error-messages="totalBookCountErrors"
                                  @input="$v.form.totalBookCount.$touch()"
                                  @blur="$v.form.totalBookCount.$touch()"
                                  @change="form.totalBookCountLock = true"
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.totalBookCountLock ? 'secondary' : ''"
                                @click="form.totalBookCountLock = !form.totalBookCountLock"
                        >
                          {{ form.totalBookCountLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-text-field>
                  </v-col>
                </v-row>

              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Alternate Titles  -->
          <v-tab-item v-if="single">
            <v-card flat min-height="100">
              <v-container fluid>
                <!-- Titles -->
                <v-form
                  v-model="titlesValid"
                  ref="titlesForm"
                >
                  <v-row
                    v-for="(title, i) in form.alternateTitles"
                    :key="i"
                  >
                    <v-col cols="4" class="py-0">
                      <v-text-field v-model="form.alternateTitles[i].label"
                                    :label="$t('dialog.edit_books.field_link_label')"
                                    filled
                                    dense
                                    :rules="[alternateTitleRules]"
                                    @input="$v.form.alternateTitles.$touch()"
                                    @blur="$v.form.alternateTitles.$touch()"
                                    @change="form.alternateTitlesLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.alternateTitlesLock ? 'secondary' : ''"
                                  @click="form.alternateTitlesLock = !form.alternateTitlesLock"
                          >
                            {{ form.linksLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>

                    <v-col cols="8" class="py-0">
                      <v-text-field v-model="form.alternateTitles[i].title"
                                    :label="$t('dialog.edit_books.field_alternate_title')"
                                    filled
                                    dense
                                    :rules="[alternateTitleRules]"
                                    @input="$v.form.alternateTitles.$touch()"
                                    @blur="$v.form.alternateTitles.$touch()"
                                    @change="form.alternateTitlesLock = true"
                      >
                        <template v-slot:append-outer>
                          <v-icon @click="form.alternateTitles.splice(i, 1)">mdi-delete</v-icon>
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
                      @click="form.alternateTitles.push({label:'', title:''})"
                    >
                      <v-icon>mdi-plus</v-icon>
                    </v-btn>
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
                >{{ $t('dialog.edit_series.tags_notice_multiple_edit') }}
                </v-alert>

                <!-- Genres -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_series.field_genres') }}</span>
                    <v-combobox v-model="form.genres"
                                :items="genresAvailable"
                                @input="$v.form.genres.$touch()"
                                @change="form.genresLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.genresLock ? 'secondary' : ''"
                                @click="form.genresLock = !form.genresLock"
                        >
                          {{ form.genresLock ? 'mdi-lock' : 'mdi-lock-open' }}
                        </v-icon>
                      </template>
                    </v-combobox>
                  </v-col>
                </v-row>

                <!-- Tags -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_series.field_tags') }}</span>
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
                    v-for="(link, i) in form.links"
                    :key="i"
                  >
                    <v-col cols="4" class="py-0">
                      <v-text-field v-model="form.links[i].label"
                                    :label="$t('dialog.edit_books.field_link_label')"
                                    filled
                                    dense
                                    :rules="[linksLabelRules]"
                                    @input="$v.form.links.$touch()"
                                    @blur="$v.form.links.$touch()"
                                    @change="form.linksLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.linksLock ? 'secondary' : ''"
                                  @click="form.linksLock = !form.linksLock"
                          >
                            {{ form.linksLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>

                    <v-col cols="8" class="py-0">
                      <v-text-field v-model="form.links[i].url"
                                    :label="$t('dialog.edit_books.field_link_url')"
                                    filled
                                    dense
                                    :rules="[linksUrlRules]"
                                    @input="$v.form.links.$touch()"
                                    @blur="$v.form.links.$touch()"
                                    @change="form.linksLock = true"
                      >
                        <template v-slot:append-outer>
                          <v-icon @click="form.links.splice(i, 1)">mdi-delete</v-icon>
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
                      @click="form.links.push({label:'', url:''})"
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
                    v-for="(item, index) in [...poster.uploadQueue, ...poster.seriesThumbnails]"
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
                >{{ $t('dialog.edit_series.tags_notice_multiple_edit') }}
                </v-alert>

                <!-- Sharing Labels -->
                <v-row>
                  <v-col cols="12">
                    <span class="text-body-2">{{ $t('dialog.edit_series.field_labels') }}</span>
                    <v-combobox v-model="form.sharingLabels"
                                :items="sharingLabelsAvailable"
                                @input="$v.form.sharingLabels.$touch()"
                                @change="form.sharingLabelsLock = true"
                                hide-selected
                                chips
                                deletable-chips
                                multiple
                                filled
                                dense
                    >
                      <template v-slot:prepend>
                        <v-icon :color="form.sharingLabelsLock ? 'secondary' : ''"
                                @click="form.sharingLabelsLock = !form.sharingLabelsLock"
                        >
                          {{ form.sharingLabelsLock ? 'mdi-lock' : 'mdi-lock-open' }}
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
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_series.button_cancel') }}</v-btn>
          <v-btn color="primary" @click="dialogConfirm">{{ $t('dialog.edit_series.button_confirm') }}</v-btn>
        </v-card-actions>
      </v-card>
    </form>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {SeriesStatus} from '@/types/enum-series'
import {helpers, minValue, requiredIf} from 'vuelidate/lib/validators'
import {ReadingDirection} from '@/types/enum-books'
import {SeriesDto, SeriesThumbnailDto} from '@/types/komga-series'
import {ERROR, ErrorEvent} from '@/types/events'
import DropZone from '@/components/DropZone.vue'
import ThumbnailCard from '@/components/ThumbnailCard.vue'

const tags = require('language-tags')

const validLanguage = (value: string) => !helpers.req(value) || tags.check(value)

export default Vue.extend({
  name: 'EditSeriesDialog',
  components: {ThumbnailCard, DropZone},
  data: () => {
    return {
      modal: false,
      tab: 0,
      linksValid: false,
      titlesValid: false,
      form: {
        status: '',
        statusLock: false,
        title: '',
        titleLock: false,
        titleSort: '',
        titleSortLock: false,
        summary: '',
        summaryLock: false,
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
        tags: [],
        tagsLock: false,
        totalBookCount: undefined as number | undefined,
        totalBookCountLock: false,
        sharingLabels: [],
        sharingLabelsLock: false,
        links: [],
        linksLock: false,
        alternateTitles: [],
        alternateTitlesLock: false,
      },
      mixed: {
        status: false,
        readingDirection: false,
        publisher: false,
        ageRating: false,
        language: false,
      },
      poster: {
        selectedThumbnail: '',
        uploadQueue: [] as File[],
        deleteQueue: [] as SeriesThumbnailDto[],
        seriesThumbnails: [] as SeriesThumbnailDto[],
      },
      genresAvailable: [] as string[],
      tagsAvailable: [] as string[],
      sharingLabelsAvailable: [] as string[],
    }
  },
  props: {
    value: Boolean,
    series: {
      type: [Object as () => SeriesDto, Array as () => SeriesDto[]],
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      if(val) {
        this.getThumbnails(this.series)
        this.loadAvailableTags()
        this.loadAvailableGenres()
        this.loadAvailableSharingLabels()
      } else {
        this.dialogCancel()
      }
    },
    series(val) {
      this.dialogReset(val)
    },
  },
  validations: {
    form: {
      title: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      titleSort: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      status: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        }),
      },
      summary: {},
      language: {
        validLanguage: validLanguage,
      },
      genres: {},
      tags: {},
      sharingLabels: {},
      ageRating: {minValue: minValue(0)},
      readingDirection: {},
      publisher: {},
      totalBookCount: {minValue: minValue(1)},
      links: {},
      alternateTitles: {},
    },
  },
  computed: {
    single(): boolean {
      return !Array.isArray(this.series)
    },
    readingDirections(): any[] {
      return Object.keys(ReadingDirection).map(x => (
        {
          text: this.$t(`enums.reading_direction.${x}`),
          value: x,
        }),
      )
    },
    seriesStatus(): any[] {
      return Object.keys(SeriesStatus).map(x => ({
        text: this.$t(`enums.series_status.${x}`),
        value: x,
      }))
    },
    ageRatingErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.ageRating?.$dirty) return errors
      !this.$v?.form?.ageRating?.minValue && errors.push(this.$t('dialog.edit_series.field_age_rating_error').toString())
      return errors
    },
    totalBookCountErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.totalBookCount?.$dirty) return errors
      !this.$v?.form?.totalBookCount?.minValue && errors.push(this.$t('dialog.edit_series.field_total_book_count_error').toString())
      return errors
    },
    languageErrors(): string[] {
      if (!this.$v.form?.language?.$dirty) return []
      if (!this.$v?.form?.language?.validLanguage) return tags(this.form.language).errors().map((x: any) => x.message)
      return []
    },
    dialogTitle(): string {
      return this.single
        ? this.$t('dialog.edit_series.dialog_title_single', {series: this.$_.get(this.series, 'metadata.title')}).toString()
        : this.$tc('dialog.edit_series.dialog_title_multiple', (this.series as SeriesDto[]).length)
    },
  },
  methods: {
    alternateTitleRules(text: string): boolean | string {
      if (!!this.$_.trim(text)) return true
      return this.$t('common.required').toString()
    },
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
    async loadAvailableTags() {
      this.tagsAvailable = await this.$komgaReferential.getTags()
    },
    async loadAvailableGenres() {
      this.genresAvailable = await this.$komgaReferential.getGenres()
    },
    async loadAvailableSharingLabels() {
      this.sharingLabelsAvailable = await this.$komgaReferential.getSharingLabels()
    },
    requiredErrors(fieldName: string): string[] {
      const errors = [] as string[]
      const formField = this.$v.form!![fieldName] as any
      if (!formField.$dirty) return errors
      !formField.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    dialogReset(series: SeriesDto | SeriesDto[]) {
      this.tab = 0
      this.$v.$reset();
      (this.$refs.linksForm as any)?.resetValidation();
      (this.$refs.titlesForm as any)?.resetValidation()
      if (Array.isArray(series) && series.length === 0) return
      if (Array.isArray(series) && series.length > 0) {
        const status = this.$_.uniq(series.map(x => x.metadata.status))
        this.form.status = status.length > 1 ? '' : status[0]
        this.mixed.status = status.length > 1

        const statusLock = this.$_.uniq(series.map(x => x.metadata.statusLock))
        this.form.statusLock = statusLock.length > 1 ? false : statusLock[0]

        const readingDirection = this.$_.uniq(series.map(x => x.metadata.readingDirection))
        this.form.readingDirection = readingDirection.length > 1 ? '' : readingDirection[0]
        this.mixed.readingDirection = readingDirection.length > 1

        const readingDirectionLock = this.$_.uniq(series.map(x => x.metadata.readingDirectionLock))
        this.form.readingDirectionLock = readingDirectionLock.length > 1 ? false : readingDirectionLock[0]

        const ageRating = this.$_.uniq(series.map(x => x.metadata.ageRating))
        this.form.ageRating = ageRating.length > 1 ? undefined : ageRating[0]
        this.mixed.ageRating = ageRating.length > 1

        const ageRatingLock = this.$_.uniq(series.map(x => x.metadata.ageRatingLock))
        this.form.ageRatingLock = ageRatingLock.length > 1 ? false : ageRatingLock[0]

        const publisher = this.$_.uniq(series.map(x => x.metadata.publisher))
        this.form.publisher = publisher.length > 1 ? '' : publisher[0]
        this.mixed.publisher = publisher.length > 1

        const publisherLock = this.$_.uniq(series.map(x => x.metadata.publisherLock))
        this.form.publisherLock = publisherLock.length > 1 ? false : publisherLock[0]

        const language = this.$_.uniq(series.map(x => x.metadata.language))
        this.form.language = language.length > 1 ? '' : language[0]
        this.mixed.language = language.length > 1

        const languageLock = this.$_.uniq(series.map(x => x.metadata.languageLock))
        this.form.languageLock = languageLock.length > 1 ? false : languageLock[0]

        this.form.genres = []

        const genresLock = this.$_.uniq(series.map(x => x.metadata.genresLock))
        this.form.genresLock = genresLock.length > 1 ? false : genresLock[0]

        this.form.tags = []

        const tagsLock = this.$_.uniq(series.map(x => x.metadata.tagsLock))
        this.form.tagsLock = tagsLock.length > 1 ? false : tagsLock[0]

        this.form.sharingLabels = []

        const sharingLabelsLock = this.$_.uniq(series.map(x => x.metadata.sharingLabelsLock))
        this.form.sharingLabelsLock = sharingLabelsLock.length > 1 ? false : sharingLabelsLock[0]

        this.form.links = []
        this.form.alternateTitles = []
      } else {
        this.form.genres = []
        this.form.tags = []
        this.form.sharingLabels = []
        this.form.links = []
        this.form.alternateTitles = []
        this.$_.merge(this.form, (series as SeriesDto).metadata)
        this.poster.selectedThumbnail = ''
        this.poster.deleteQueue = []
        this.poster.uploadQueue = []
        this.poster.seriesThumbnails = []
      }
    },
    dialogCancel() {
      this.$emit('input', false)
      this.dialogReset(this.series)
    },
    async dialogConfirm() {
      if (await this.editSeries()) {
        this.$emit('input', false)
      }
    },
    validateForm(): any {
      if (!this.$v.$invalid
        && (!this.single || !this.$refs.linksForm || (this.$refs.linksForm as any).validate())
        && (!this.single || !this.$refs.titlesForm || (this.$refs.titlesForm as any).validate())
      ) {
        const metadata = {
          statusLock: this.form.statusLock,
          readingDirectionLock: this.form.readingDirectionLock,
          ageRatingLock: this.form.ageRatingLock,
          publisherLock: this.form.publisherLock,
          languageLock: this.form.languageLock,
          genresLock: this.form.genresLock,
          tagsLock: this.form.tagsLock,
          totalBookCountLock: this.form.totalBookCountLock,
          sharingLabelsLock: this.form.sharingLabelsLock,
          linksLock: this.form.linksLock,
          alternateTitlesLock: this.form.alternateTitlesLock,
        }

        if (this.$v.form?.status?.$dirty) {
          this.$_.merge(metadata, {status: this.form.status})
        }

        if (this.$v.form?.readingDirection?.$dirty) {
          this.$_.merge(metadata, {readingDirection: this.form.readingDirection ? this.form.readingDirection : null})
        }

        if (this.$v.form?.ageRating?.$dirty) {
          this.$_.merge(metadata, {ageRating: this.form.ageRating})
        }

        if (this.$v.form?.publisher?.$dirty) {
          this.$_.merge(metadata, {publisher: this.form.publisher})
        }

        if (this.$v.form?.genres?.$dirty) {
          this.$_.merge(metadata, {genres: this.form.genres})
        }

        if (this.$v.form?.tags?.$dirty) {
          this.$_.merge(metadata, {tags: this.form.tags})
        }

        if (this.$v.form?.language?.$dirty) {
          this.$_.merge(metadata, {language: this.form.language})
        }

        if (this.$v.form?.sharingLabels?.$dirty) {
          this.$_.merge(metadata, {sharingLabels: this.form.sharingLabels})
        }

        if (this.single) {
          this.$_.merge(metadata, {
            titleLock: this.form.titleLock,
            titleSortLock: this.form.titleSortLock,
            summaryLock: this.form.summaryLock,
            totalBookCountLock: this.form.totalBookCountLock,
          })

          if (this.$v.form?.title?.$dirty) {
            this.$_.merge(metadata, {title: this.form.title})
          }

          if (this.$v.form?.titleSort?.$dirty) {
            this.$_.merge(metadata, {titleSort: this.form.titleSort})
          }

          if (this.$v.form?.summary?.$dirty) {
            this.$_.merge(metadata, {summary: this.form.summary})
          }

          if (this.$v.form?.totalBookCount?.$dirty) {
            this.$_.merge(metadata, {totalBookCount: this.form.totalBookCount})
          }

          if (this.$v.form?.links?.$dirty || this.form.links.length != (this.series as SeriesDto).metadata.links?.length) {
            this.$_.merge(metadata, {links: this.form.links})
          }

          if (this.$v.form?.alternateTitles?.$dirty || this.form.alternateTitles.length != (this.series as SeriesDto).metadata.alternateTitles?.length) {
            this.$_.merge(metadata, {alternateTitles: this.form.alternateTitles})
          }
        }

        return metadata
      }
      return null
    },
    async editSeries(): Promise<boolean> {
      if (this.single && this.poster.uploadQueue.length > 0) {
        const series = this.series as SeriesDto
        let hadErrors = false
        for (const file of this.poster.uploadQueue.slice()) {
          try {
            await this.$komgaSeries.uploadThumbnail(series.id, file, file.name === this.poster.selectedThumbnail)
            this.deleteThumbnail(file)
          } catch (e) {
            this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
            hadErrors = true
          }
        }
        if (hadErrors) {
          await this.getThumbnails(series)
          return false
        }
      }

      if (this.single && this.poster.selectedThumbnail !== '') {
        const id = this.poster.selectedThumbnail
        const series = this.series as SeriesDto
        if (this.poster.seriesThumbnails.find(value => value.id === id)) {
          this.$komgaSeries.markThumbnailAsSelected(series.id, id)
        }
      }

      if (this.single && this.poster.deleteQueue.length > 0) {
        this.poster.deleteQueue.forEach(toDelete => this.$komgaSeries.deleteThumbnail(toDelete.seriesId, toDelete.id))
      }

      const metadata = this.validateForm()
      if (metadata) {
        const toUpdate = (this.single ? [this.series] : this.series) as SeriesDto[]
        for (const s of toUpdate) {
          try {
            await this.$komgaSeries.updateMetadata(s.id, metadata)
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
    async getThumbnails(series: SeriesDto | SeriesDto[]) {
      if (Array.isArray(series)) return

      const thumbnails = await this.$komgaSeries.getThumbnails(series.id)

      this.selectThumbnail(thumbnails.find(x => x.selected))

      this.poster.seriesThumbnails = thumbnails
    },
    isThumbnailSelected(item: File | SeriesThumbnailDto): boolean {
      return item instanceof File ? item.name === this.poster.selectedThumbnail : item.id === this.poster.selectedThumbnail
    },
    selectThumbnail(item: File | SeriesThumbnailDto | undefined) {
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
    isThumbnailToBeDeleted(item: File | SeriesThumbnailDto) {
      if (item instanceof File) {
        return false
      } else {
        return this.poster.deleteQueue.includes(item)
      }
    },
    deleteThumbnail(item: File | SeriesThumbnailDto) {
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
