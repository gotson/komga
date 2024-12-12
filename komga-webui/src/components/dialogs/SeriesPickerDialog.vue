<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
              scrollable
    >
      <v-card>
        <v-card-title>{{ $t('dialog.series_picker.title') }}</v-card-title>
        <v-btn icon absolute top right @click="dialogClose">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-divider/>

        <v-card-text style="height: 50%">
          <v-container fluid>

            <v-row align="center">
              <v-col>
                <v-text-field
                  v-model="search"
                  autofocus
                  :label="$t('dialog.series_picker.label_search_series')"
                />
              </v-col>
            </v-row>

            <v-divider/>

            <v-row v-if="results">
              <v-col>
                <v-list elevation="5" v-if="results.length > 0" three-line>
                  <div v-for="(s, index) in results"
                       :key="index"
                  >
                    <v-list-item @click="select(s)">
                      <v-img :src="seriesThumbnailUrl(s.id)"
                             height="50"
                             max-width="35"
                             class="my-1 mx-3"
                             contain
                      />
                      <v-list-item-content>
                        <v-list-item-title>{{ s.metadata.title }}</v-list-item-title>
                        <v-list-item-subtitle>{{
                            $t('searchbox.in_library', {library: getLibraryName(s)})
                          }}
                        </v-list-item-subtitle>
                        <v-list-item-subtitle v-if="s.booksMetadata.releaseDate">{{
                            new Intl.DateTimeFormat($i18n.locale, {
                              year: 'numeric',
                              timeZone: 'UTC'
                            }).format(new Date(s.booksMetadata.releaseDate))
                          }}
                        </v-list-item-subtitle>
                      </v-list-item-content>
                    </v-list-item>
                    <v-divider v-if="index !== results.length-1"/>
                  </div>
                </v-list>

                <v-alert
                  v-if="results.length === 0 && showResults"
                  type="info"
                  text
                >{{ $t('dialog.series_picker.no_results') }}
                </v-alert>

              </v-col>
            </v-row>

          </v-container>
        </v-card-text>

      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {SeriesDto} from '@/types/komga-series'
import {debounce} from 'lodash'
import {seriesThumbnailUrl} from '@/functions/urls'
import {SearchConditionOneShot, SearchOperatorIsFalse, SeriesSearch} from '@/types/komga-search'

export default Vue.extend({
  name: 'SeriesPickerDialog',
  data: () => {
    return {
      modal: false,
      results: [] as SeriesDto[],
      search: '',
      showResults: false,
      seriesThumbnailUrl,
    }
  },
  props: {
    value: Boolean,
    series: {
      type: Object as PropType<SeriesDto>,
      required: false,
    },
    includeOneshots: {
      type: Boolean,
      default: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
      if (val) {
        this.clear()
      }
    },
    search(val) {
      this.searchItems(val)
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  methods: {
    searchItems: debounce(async function (this: any, query: string) {
      if (query) {
        this.showResults = false
        this.results = (await this.$komgaSeries.getSeriesList({
          fullTextSearch: query,
          condition: this.includeOneshots ? undefined : new SearchConditionOneShot(new SearchOperatorIsFalse()),
        } as SeriesSearch, {unpaged: true})).content
        this.showResults = true
      } else {
        this.clear()
      }
    }, 500),
    clear() {
      this.search = ''
      this.showResults = false
      this.results = []
    },
    select(s: SeriesDto) {
      this.$emit('update:series', s)
      this.dialogClose()
    },
    dialogClose() {
      this.$emit('input', false)
    },
    getLibraryName(item: SeriesDto): string {
      return this.$store.getters.getLibraryById(item.libraryId).name
    },
  },
})
</script>

<style scoped>

</style>
