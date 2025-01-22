<template>
  <div v-if="!$_.isEmpty(series)">
    <toolbar-sticky v-if="selectedBooks.length === 0">
      <!--   Go back to parent library   -->
      <v-tooltip bottom :disabled="!isAdmin">
        <template v-slot:activator="{ on }">
          <v-btn icon
                 v-on="on"
                 :to="parentLocation"
          >
            <rtl-icon icon="mdi-arrow-left" rtl="mdi-arrow-right"/>
          </v-btn>
        </template>
        <span v-if="contextCollection">{{ $t('common.go_to_collection') }}</span>
        <span v-else>{{ $t('common.go_to_library') }}</span>
      </v-tooltip>

      <series-actions-menu v-if="series"
                           :series="series"
      />
      <v-toolbar-title>
        <span v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</span>
        <v-chip label class="mx-4" v-if="totalElements">
          <span style="font-size: 1.1rem">{{ totalElements }}</span>
        </v-chip>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="editSeries" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>

      <page-size-select v-model="pageSize"/>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="sortOrFilterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
      </v-btn>
    </toolbar-sticky>

    <multi-select-bar
      v-model="selectedBooks"
      kind="books"
      show-select-all
      @unselect-all="selectedBooks = []"
      @select-all="selectedBooks = books"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-readlist="addToReadList"
      @bulk-edit="bulkEditMultipleBooks"
      @edit="editMultipleBooks"
      @delete="deleteBooks"
    />

    <filter-drawer
      v-model="drawer"
      :clear-button="sortOrFilterActive"
      @clear="resetSortAndFilters"
    >
      <template v-slot:default>
        <filter-list
          :filters-options="filterOptionsList"
          :filters-active.sync="filters"
        />
      </template>

      <template v-slot:filter>
        <filter-panels
          :filters-options="filterOptionsPanel"
          :filters-active.sync="filters"
          :filters-active-mode.sync="filtersMode"
        />
      </template>

      <template v-slot:sort>
        <sort-list
          :sort-default="sortDefault"
          :sort-options="sortOptions"
          :sort-active.sync="sortActive"
        />
      </template>
    </filter-drawer>

    <v-container fluid class="pa-6">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <item-card
            v-if="series.hasOwnProperty('id')"
            width="212"
            :item="series"
            thumbnail-only
            no-link
            :action-menu="false"
          ></item-card>
        </v-col>

        <v-col cols="8">
          <v-container>
            <v-row>
              <v-col class="py-1">
                <span class="text-h5" v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</span>
                <router-link
                  class="caption link-underline"
                  :class="$vuetify.breakpoint.smAndUp ? 'mx-2' : ''"
                  :style="$vuetify.breakpoint.xsOnly ? 'display: block' : ''"
                  :to="{name:'browse-libraries', params: {libraryId: series.libraryId }}"
                >{{ $t('searchbox.in_library', {library: getLibraryName(series)}) }}
                </router-link>
              </v-col>
            </v-row>

            <v-row v-if="series.booksMetadata.releaseDate" class="align-center text-caption">
              <v-col class="py-1">
                <v-tooltip right>
                  <template v-slot:activator="{ on }">
                  <span v-on="on">{{
                      new Intl.DateTimeFormat($i18n.locale, {
                        year: 'numeric',
                        timeZone: 'UTC'
                      }).format(new Date(series.booksMetadata.releaseDate))
                    }}</span>
                  </template>
                  {{ $t('browse_series.earliest_year_from_release_dates') }}
                </v-tooltip>
              </v-col>
            </v-row>

            <v-row class="text-body-2">
              <v-col class="py-1 pe-0" cols="auto">
                <v-chip label small link :color="statusChip.color" :text-color="statusChip.text"
                        :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {status: [new SearchConditionSeriesStatus(new SearchOperatorIs(series.metadata.status))]}}">
                  {{ $t(`enums.series_status.${series.metadata.status}`) }}
                </v-chip>
              </v-col>
              <v-col class="py-1 pe-0" cols="auto" v-if="series.metadata.ageRating">
                <v-chip label small link
                        :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {ageRating: [new SearchConditionAgeRating(new SearchOperatorIs(series.metadata.ageRating.toString()))]}}"
                >
                  {{ series.metadata.ageRating }}+
                </v-chip>
              </v-col>
              <v-col class="py-1 pe-0" cols="auto" v-if="series.metadata.language">
                <v-chip label small link
                        :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {language: [new SearchConditionLanguage(new SearchOperatorIs(series.metadata.language))]}}"
                >
                  {{ languageDisplay }}
                </v-chip>
              </v-col>
              <v-col class="py-1 pe-0" cols="auto"
                     v-if="series.metadata.readingDirection">
                <v-chip label small>
                  {{ $t(`enums.reading_direction.${series.metadata.readingDirection}`) }}
                </v-chip>
              </v-col>
              <v-col class="py-1 pe-0" cols="auto" v-if="unavailable">
                <v-chip label small color="error">
                  {{ $t('common.unavailable') }}
                </v-chip>
              </v-col>
            </v-row>

            <v-row class="text-caption" align="center">
              <v-col cols="auto" v-if="series.metadata.totalBookCount">
                {{ $t('common.books_total', {count: series.booksCount, total: series.metadata.totalBookCount}) }}
              </v-col>

              <v-col cols="auto" v-else>
                {{ $tc('common.books_n', series.booksCount) }}
              </v-col>
            </v-row>

            <template v-if="$vuetify.breakpoint.smAndUp">
              <!-- Alternate titles  -->
              <read-more v-model="readMoreTitles"
                         class="mb-4"
                         i18n-less="titles_more.less"
                         i18n-more="titles_more.more"
                         v-if="series.metadata.alternateTitles.length > 0"
              >
                <v-row v-for="(a, i) in series.metadata.alternateTitles"
                       :key="i"
                       class="align-center text-caption"
                >
                  <v-col cols="4" sm="3" md="2" xl="1" class="py-0 text-uppercase"
                         :class="i===0 ? 'pt-4' : i === series.metadata.alternateTitles.length - 1 ? 'pb-4' : ''">
                    {{ a.label }}
                  </v-col>
                  <v-col cols="8" sm="9" md="10" xl="11" class="py-0"
                         :class="i===0 ? 'pt-4' : i === series.metadata.alternateTitles.length - 1 ? 'pb-4' : ''">
                    {{ a.title }}
                  </v-col>
                </v-row>
              </read-more>

              <v-row class="align-center">
                <v-col cols="auto">
                  <v-btn :title="$t('menu.download_series')"
                         small
                         :disabled="!canDownload"
                         :href="fileUrl">
                    <v-icon left small>mdi-file-download</v-icon>
                    {{ $t('common.download') }}
                  </v-btn>
                </v-col>
              </v-row>

              <v-row v-if="series.metadata.summary">
                <v-col>
                  <read-more v-model="readMore">{{ series.metadata.summary }}</read-more>
                </v-col>
              </v-row>

              <v-row v-if="!series.metadata.summary && series.booksMetadata.summary">
                <v-col>
                  <v-tooltip right>
                    <template v-slot:activator="{ on }">
                  <span v-on="on" class="text-caption">
                    {{ $t('browse_series.summary_from_book', {number: series.booksMetadata.summaryNumber}) }}
                  </span>
                    </template>
                    {{ $t('browse_series.series_no_summary') }}
                  </v-tooltip>
                  <read-more v-model="readMore">{{ series.booksMetadata.summary }}</read-more>
                </v-col>
              </v-row>
            </template>
          </v-container>
        </v-col>
      </v-row>

      <template v-if="$vuetify.breakpoint.xsOnly">
        <!-- Alternate titles  -->
        <read-more v-model="readMoreTitles"
                   class="mb-4"
                   i18n-less="titles_more.less"
                   i18n-more="titles_more.more"
                   v-if="series.metadata.alternateTitles.length > 0"
        >
          <v-row v-for="(a, i) in series.metadata.alternateTitles"
                 :key="i"
                 class="align-center text-caption"
          >
            <v-col cols="4" class="py-0 text-uppercase"
                   :class="i===0 ? 'pt-4' : i === series.metadata.alternateTitles.length - 1 ? 'pb-4' : ''">{{
                a.label
              }}
            </v-col>
            <v-col cols="8" class="py-0"
                   :class="i===0 ? 'pt-4' : i === series.metadata.alternateTitles.length - 1 ? 'pb-4' : ''">{{
                a.title
              }}
            </v-col>
          </v-row>
        </read-more>

        <!--   Download button     -->
        <v-row class="align-center">
          <v-col cols="auto">
            <v-btn :title="$t('menu.download_series')"
                   small
                   :disabled="!canDownload"
                   :href="fileUrl">
              <v-icon left small>mdi-file-download</v-icon>
              {{ $t('common.download') }}
            </v-btn>
          </v-col>
        </v-row>

        <!--   Series summary     -->
        <v-row v-if="series.metadata.summary">
          <v-col>
            <read-more v-model="readMore">{{ series.metadata.summary }}</read-more>
          </v-col>
        </v-row>

        <!--   Series summary from books     -->
        <v-row v-if="!series.metadata.summary && series.booksMetadata.summary">
          <v-col>
            <v-tooltip right>
              <template v-slot:activator="{ on }">
                  <span v-on="on" class="text-caption">
                    {{ $t('browse_series.summary_from_book', {number: series.booksMetadata.summaryNumber}) }}
                  </span>
              </template>
              {{ $t('browse_series.series_no_summary') }}
            </v-tooltip>
            <read-more v-model="readMore">{{ series.booksMetadata.summary }}</read-more>
          </v-col>
        </v-row>
      </template>

      <!--  Publisher    -->
      <v-row v-if="series.metadata.publisher" class="align-center text-caption">
        <v-col cols="4" sm="3" md="2" xl="1" class="py-1 text-uppercase">{{ $t('common.publisher') }}</v-col>
        <v-col cols="8" sm="9" md="10" xl="11" class="py-1">
          <v-chip
            class="me-2"
            :title="series.metadata.publisher"
            :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {publisher: [new SearchConditionPublisher(new SearchOperatorIs(series.metadata.publisher))]}}"
            label
            small
            outlined
            link
          >{{ series.metadata.publisher }}
          </v-chip>
        </v-col>
      </v-row>

      <!--  Genres    -->
      <v-row v-if="series.metadata.genres.length > 0" class="align-center text-caption">
        <v-col cols="4" sm="3" md="2" xl="1" class="py-1 text-uppercase">{{ $t('common.genre') }}</v-col>
        <v-col cols="8" sm="9" md="10" xl="11" class="py-1 text-capitalize">
          <vue-horizontal>
            <template v-slot:btn-prev>
              <v-btn icon small>
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>
            </template>

            <template v-slot:btn-next>
              <v-btn icon small>
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </template>
            <v-chip v-for="(t, i) in $_.sortBy(series.metadata.genres)"
                    :key="i"
                    class="me-2"
                    :title="t"
                    :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {genre: [new SearchConditionGenre(new SearchOperatorIs(t))]}}"
                    label
                    small
                    outlined
                    link
            >{{ t }}
            </v-chip>
          </vue-horizontal>
        </v-col>
      </v-row>

      <!--  Tags    -->
      <v-row v-if="series.metadata.tags.length > 0 || series.booksMetadata.tags.length > 0"
             class="align-center text-caption">
        <v-col cols="4" sm="3" md="2" xl="1" class="py-1 text-uppercase">{{ $t('common.tags') }}</v-col>
        <v-col cols="8" sm="9" md="10" xl="11" class="py-1 text-capitalize">
          <vue-horizontal>
            <template v-slot:btn-prev>
              <v-btn icon small>
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>
            </template>

            <template v-slot:btn-next>
              <v-btn icon small>
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </template>
            <v-chip v-for="(t, i) in $_.sortBy(series.metadata.tags)"
                    :key="`series_${i}`"
                    class="me-2"
                    :title="t"
                    :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {tag: [new SearchConditionTag(new SearchOperatorIs(t))]}}"
                    label
                    small
                    outlined
                    link
            >{{ t }}
            </v-chip>
            <v-chip v-for="(t, i) in $_(series.booksMetadata.tags).difference(series.metadata.tags).sortBy()"
                    :key="`book_${i}`"
                    class="me-2"
                    :title="t"
                    :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {tag: [new SearchConditionTag(new SearchOperatorIs(t))]}}"
                    label
                    small
                    outlined
                    link
                    color="contrast-light-2"
            >{{ t }}
            </v-chip>
          </vue-horizontal>
        </v-col>
      </v-row>

      <v-row v-if="series.metadata.links.length > 0" class="align-center text-caption">
        <v-col class="py-1 text-uppercase" cols="4" sm="3" md="2" xl="1">{{ $t('browse_book.links') }}</v-col>
        <v-col class="py-1" cols="8" sm="9" md="10" xl="11">
          <v-chip
            v-for="(link, i) in series.metadata.links"
            :href="link.url"
            rel="noreferrer"
            target="_blank"
            class="me-2"
            label
            small
            outlined
            link
            :key="i"
          >
            {{ link.label }}
            <v-icon
              x-small
              color="grey"
              class="ps-1"
            >
              mdi-open-in-new
            </v-icon>
          </v-chip>
        </v-col>
      </v-row>

      <v-divider v-if="series.booksMetadata.authors.length > 0" class="my-3"/>
      <v-row class="align-center text-caption"
             v-for="role in displayedRoles"
             :key="role"
      >
        <v-col cols="4" sm="3" md="2" xl="1" class="py-1 text-uppercase">{{ $t(`author_roles.${role}`) }}</v-col>
        <v-col cols="8" sm="9" md="10" xl="11" class="py-1">
          <vue-horizontal>
            <template v-slot:btn-prev>
              <v-btn icon small>
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>
            </template>

            <template v-slot:btn-next>
              <v-btn icon small>
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </template>

            <v-chip v-for="(name, i) in authorsByRole[role].sort()"
                    :key="i"
                    class="me-2"
                    :title="name"
                    :to="{name:'browse-libraries', params: {libraryId: series.libraryId }, query: {[role]: [name]}}"
                    label
                    small
                    outlined
                    link
            >{{ name }}
            </v-chip>
          </vue-horizontal>
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <collections-expansion-panels :collections="collections">
            <template v-slot:prepend="props">
              <v-tooltip bottom>
                <template v-slot:activator="{ on }">
                  <v-btn icon class="me-2" v-on="on" @click="removeFromCollection(props.collection.id)">
                    <v-icon>mdi-playlist-remove</v-icon>
                  </v-btn>
                </template>
                <span>{{ $t('browse_series.remove_from_collection') }}</span>
              </v-tooltip>
            </template>
          </collections-expansion-panels>
        </v-col>
      </v-row>

      <v-divider class="mt-4 mb-1"/>

      <empty-state
        v-if="totalPages === 0"
        :title="$t('common.filter_no_matches')"
        :sub-title="$t('common.use_filter_panel_to_change_filter')"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
        <v-btn @click="resetSortAndFilters">{{ $t('common.reset_filters') }}</v-btn>
      </empty-state>

      <template v-else>
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser :items="books"
                      :item-context="itemContext"
                      :selected.sync="selectedBooks"
                      :edit-function="isAdmin ? editSingleBook : undefined"
        />

        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />
      </template>

    </v-container>

  </div>
</template>

<script lang="ts">
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import CollectionsExpansionPanels from '@/components/CollectionsExpansionPanels.vue'
import EmptyState from '@/components/EmptyState.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ItemCard from '@/components/ItemCard.vue'
import SeriesActionsMenu from '@/components/menus/SeriesActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {parseQuerySort} from '@/functions/query-params'
import {seriesFileUrl, seriesThumbnailUrl} from '@/functions/urls'
import {MediaProfile, ReadStatus} from '@/types/enum-books'
import {
  BOOK_ADDED,
  BOOK_CHANGED,
  BOOK_DELETED,
  COLLECTION_ADDED,
  COLLECTION_CHANGED,
  COLLECTION_DELETED,
  LIBRARY_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
  SERIES_CHANGED,
  SERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {BookDto} from '@/types/komga-books'
import {SeriesStatus} from '@/types/enum-series'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterList from '@/components/FilterList.vue'
import SortList from '@/components/SortList.vue'
import {
  extractFilterOptionsValues,
  mergeFilterParams,
  sortOrFilterActive,
  toNameValueCondition,
} from '@/functions/filter'
import FilterPanels from '@/components/FilterPanels.vue'
import {SeriesDto} from '@/types/komga-series'
import {groupAuthorsByRole} from '@/functions/authors'
import ReadMore from '@/components/ReadMore.vue'
import {authorRoles, authorRolesSeries} from '@/types/author-roles'
import VueHorizontal from 'vue-horizontal'
import RtlIcon from '@/components/RtlIcon.vue'
import {throttle} from 'lodash'
import {BookSseDto, CollectionSseDto, LibrarySseDto, ReadProgressSseDto, SeriesSseDto} from '@/types/komga-sse'
import {ItemContext} from '@/types/items'
import {Context, ContextOrigin} from '@/types/context'
import {RawLocation} from 'vue-router/types/router'
import {
  SearchConditionAgeRating,
  SearchConditionAllOfBook,
  SearchConditionAnyOfBook,
  SearchConditionAuthor,
  SearchConditionBook,
  SearchConditionGenre,
  SearchConditionLanguage,
  SearchConditionMediaProfile,
  SearchConditionPublisher,
  SearchConditionReadStatus,
  SearchConditionSeriesId,
  SearchConditionSeriesStatus,
  SearchConditionTag,
  SearchOperatorIs,
  SearchOperatorIsNot,
} from '@/types/komga-search'
import {objIsEqual} from '@/functions/object'
import i18n from '@/i18n'
import {
  FILTER_ANY,
  FILTER_NONE,
  FilterMode,
  FiltersActive,
  FiltersActiveMode,
  FiltersOptions,
  NameValue,
} from '@/types/filter'

const tags = require('language-tags')

export default Vue.extend({
  name: 'BrowseSeries',
  components: {
    ToolbarSticky,
    ItemBrowser,
    PageSizeSelect,
    SeriesActionsMenu,
    ItemCard,
    EmptyState,
    MultiSelectBar,
    CollectionsExpansionPanels,
    FilterDrawer,
    FilterList,
    FilterPanels,
    SortList,
    ReadMore,
    VueHorizontal,
    RtlIcon,
  },
  data: function () {
    return {
      SearchConditionSeriesStatus,
      SearchConditionPublisher,
      SearchConditionGenre,
      SearchConditionTag,
      SearchConditionLanguage,
      SearchConditionAgeRating,
      SearchOperatorIs,
      series: {} as SeriesDto,
      context: {} as Context,
      books: [] as BookDto[],
      selectedBooks: [] as BookDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortActive: {} as SortActive,
      sortDefault: {key: 'metadata.numberSort', order: 'asc'} as SortActive,
      filters: {} as FiltersActive,
      filtersMode: {} as FiltersActiveMode,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      filterModeUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      collections: [] as CollectionDto[],
      drawer: false,
      filterOptions: {
        tag: [] as NameValue[],
        mediaProfile: [] as NameValue[],
      },
      readMore: false,
      readMoreTitles: false,
    }
  },
  computed: {
    itemContext(): ItemContext[] {
      if (this.sortActive.key === 'metadata.releaseDate') return [ItemContext.RELEASE_DATE]
      if (this.sortActive.key === 'createdDate') return [ItemContext.DATE_ADDED]
      if (this.sortActive.key === 'fileSize') return [ItemContext.FILE_SIZE]
      return []
    },
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('sort.number').toString(), key: 'metadata.numberSort'},
        {name: this.$t('sort.date_added').toString(), key: 'createdDate'},
        {name: this.$t('sort.release_date').toString(), key: 'metadata.releaseDate'},
        {name: this.$t('sort.file_size').toString(), key: 'fileSize'},
        {name: this.$t('sort.file_name').toString(), key: 'name'},
        {name: this.$t('sort.page_count').toString(), key: 'media.pagesCount'},
      ] as SortOption[]
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {
              name: this.$t('filter.unread').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.UNREAD)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.UNREAD)),
            },
            {
              name: this.$t('filter.in_progress').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.IN_PROGRESS)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.IN_PROGRESS)),
            },
            {
              name: this.$t('filter.read').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.READ)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.READ)),
            },
          ],
        },
      } as FiltersOptions
    },
    filterOptionsPanel(): FiltersOptions {
      const r = {
        tag: {name: this.$t('filter.tag').toString(), values: this.filterOptions.tag, anyAllSelector: true},
        mediaProfile: {
          name: this.$t('filter.media_profile').toString(), values: Object.values(MediaProfile).map(x => ({
            name: i18n.t(`enums.media_profile.${x}`),
            value: new SearchConditionMediaProfile(new SearchOperatorIs(x)),
            nValue: new SearchConditionMediaProfile(new SearchOperatorIsNot(x)),
          } as NameValue)),
        },
      } as FiltersOptions
      authorRoles.forEach((role: string) => {
        r[role] = {
          name: this.$t(`author_roles.${role}`).toString(),
          search: async search => {
            return (await this.$komgaReferential.getAuthors(search, role, undefined, undefined, this.seriesId))
              .content
              .map(x => x.name)
          },
          values: [{
            name: this.$t('filter.any').toString(),
            value: FILTER_ANY,
            nValue: FILTER_NONE,
          }],
          anyAllSelector: true,
        }
      })
      return r
    },
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    unavailable(): boolean {
      return this.series.deleted || this.$store.getters.getLibraryById(this.series.libraryId).unavailable
    },
    canDownload(): boolean {
      return this.$store.getters.meFileDownload && !this.unavailable
    },
    fileUrl(): string {
      return seriesFileUrl(this.seriesId)
    },
    thumbnailUrl(): string {
      return seriesThumbnailUrl(this.seriesId)
    },
    paginationVisible(): number {
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
          return 5
        case 'sm':
        case 'md':
          return 10
        case 'lg':
        case 'xl':
        default:
          return 15
      }
    },
    languageDisplay(): string {
      return tags(this.series.metadata.language)?.language()?.descriptions()[0] || this.series.metadata.language
    },
    statusChip(): object {
      switch (this.series.metadata.status) {
        case SeriesStatus.ABANDONED:
          return {color: 'red darken-4', text: 'white'}
        case SeriesStatus.ENDED:
          return {color: 'green darken-4', text: 'white'}
        case SeriesStatus.HIATUS:
          return {color: 'orange darken-4', text: 'white'}
      }
      return {color: undefined, text: undefined}
    },
    sortOrFilterActive(): boolean {
      return sortOrFilterActive(this.sortActive, this.sortDefault, this.filters)
    },
    authorsByRole(): any {
      return groupAuthorsByRole(this.series.booksMetadata.authors)
    },
    displayedRoles(): string[] {
      return authorRolesSeries.filter(x => this.authorsByRole[x])
    },
    contextCollection(): boolean {
      return this.context.origin === ContextOrigin.COLLECTION
    },
    parentLocation(): RawLocation {
      if (this.contextCollection)
        return {name: 'browse-collection', params: {collectionId: this.context.id}}
      else
        return {name: 'browse-libraries', params: {libraryId: this.series.libraryId}}
    },
  },
  props: {
    seriesId: {
      type: String,
      required: true,
    },
  },
  watch: {
    series(val) {
      if (this.$_.has(val, 'metadata.title')) {
        document.title = `Komga - ${val.metadata.title}`
      }
    },
  },
  created() {
    this.$eventHub.$on(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$on(SERIES_DELETED, this.seriesDeleted)
    this.$eventHub.$on(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.readProgressChanged)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(COLLECTION_ADDED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_DELETED, this.collectionChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$off(SERIES_DELETED, this.seriesDeleted)
    this.$eventHub.$off(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.readProgressChanged)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(COLLECTION_ADDED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_DELETED, this.collectionChanged)
  },
  async mounted() {
    this.pageSize = this.$store.state.persistedState.browsingPageSize || this.pageSize

    // restore from query param
    await this.resetParams(this.$route, this.seriesId)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadSeries(this.seriesId)

    this.setWatches()
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.seriesId !== from.params.seriesId) {
      this.unsetWatches()

      // reset
      await this.resetParams(to, to.params.seriesId)
      this.readMore = false
      this.readMoreTitles = false
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.books = []
      this.collections = []

      this.loadSeries(to.params.seriesId)

      this.setWatches()
    }

    next()
  },
  methods: {
    getLibraryName(item: SeriesDto): string {
      return this.$store.getters.getLibraryById(item.libraryId).name
    },
    resetSortAndFilters() {
      this.drawer = false
      for (const prop in this.filters) {
        this.$set(this.filters, prop, [])
      }
      this.sortActive = this.sortDefault
      this.updateRouteAndReload()
    },
    async resetParams(route: any, seriesId: string) {
      this.sortActive = this.parseQuerySortOrDefault(route.query.sort)

      // load dynamic filters
      this.$set(this.filterOptions, 'tag', toNameValueCondition(await this.$komgaReferential.getBookTags(seriesId), x => new SearchConditionTag(new SearchOperatorIs(x)), x => new SearchConditionTag(new SearchOperatorIsNot(x))))

      // get filter from query params and validate with available filter values
      let activeFilters = {} as FiltersActive
      if (route.query.readStatus || route.query.tag || route.query.mediaProfile || authorRoles.some(role => role in route.query)) {
        activeFilters = {
          readStatus: route.query.readStatus || [],
          tag: route.query.tag || [],
          mediaProfile: route.query.mediaProfile || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
        })
      }
      this.filters = this.validateFilters(activeFilters)

      // get filter mode from query params
      let activeFiltersMode = {} as FiltersActiveMode
      if (route.query.filterMode) {
        activeFiltersMode = route.query.filterMode
      }
      this.filtersMode = this.validateFiltersMode(activeFiltersMode)
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        readStatus: this.$_.intersectionWith(filters.readStatus, extractFilterOptionsValues(this.filterOptionsList.readStatus.values), objIsEqual) || [],
        tag: this.$_.intersectionWith(filters.tag, extractFilterOptionsValues(this.filterOptions.tag), objIsEqual) || [],
        mediaProfile: this.$_.intersectionWith(filters.mediaProfile, extractFilterOptionsValues(this.filterOptionsPanel.mediaProfile.values), objIsEqual) || [],
      } as any
      authorRoles.forEach((role: string) => {
        validFilter[role] = filters[role] || []
      })
      return validFilter
    },
    validateFiltersMode(filtersMode: any): FiltersActiveMode {
      const validFilterMode = {} as FiltersActiveMode
      for (let key in filtersMode) {
        if (filtersMode[key].allOf == 'true' || filtersMode[key].allOf == true) validFilterMode[key] = {allOf: true} as FilterMode
      }
      return validFilterMode
    },
    setWatches() {
      this.sortUnwatch = this.$watch('sortActive', this.updateRouteAndReload)
      this.filterUnwatch = this.$watch('filters', this.updateRouteAndReload)
      this.filterModeUnwatch = this.$watch('filtersMode', this.updateRouteAndReload)
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.seriesId, val, this.sortActive)
      })
    },
    unsetWatches() {
      this.sortUnwatch()
      this.filterUnwatch()
      this.filterModeUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.page = 1

      this.updateRoute()
      this.loadPage(this.seriesId, this.page, this.sortActive)

      this.setWatches()
    },
    libraryDeleted(event: LibrarySseDto) {
      if (event.libraryId === this.series.libraryId) {
        this.$router.push({name: 'home'})
      }
    },
    seriesChanged(event: SeriesSseDto) {
      if (event.seriesId === this.seriesId)
        this.$komgaSeries.getOneSeries(this.seriesId)
          .then(v => this.series = v)
    },
    seriesDeleted(event: SeriesSseDto) {
      if (event.seriesId === this.seriesId) {
        this.$router.push({name: 'browse-libraries', params: {libraryId: this.series.libraryId}})
      }
    },
    bookChanged(event: BookSseDto) {
      if (event.seriesId === this.seriesId) this.reloadPage()
    },
    readProgressChanged(event: ReadProgressSseDto) {
      if (this.books.some(b => b.id === event.bookId)) {
        this.reloadPage()
        this.reloadSeries()
      }
    },
    collectionChanged(event: CollectionSseDto) {
      if (event.seriesIds.includes(this.seriesId) || this.collections.map(x => x.id).includes(event.collectionId)) {
        this.$komgaSeries.getCollections(this.seriesId)
          .then(v => this.collections = v)
      }
    },
    reloadPage: throttle(function (this: any) {
      this.loadPage(this.seriesId, this.page, this.sortActive)
    }, 1000),
    reloadSeries: throttle(function (this: any) {
      this.$komgaSeries.getOneSeries(this.seriesId)
        .then((v: SeriesDto) => this.series = v)
    }, 1000),
    async loadSeries(seriesId: string) {
      this.$komgaSeries.getOneSeries(seriesId)
        .then(v => {
          this.series = v
          // for the cases where we can't change the origin target route because we don't have the full BookDto
          if (this.series.oneshot) this.$router.replace({name: 'browse-oneshot', params: {seriesId: this.seriesId}})
        })
      this.$komgaSeries.getCollections(seriesId)
        .then(v => this.collections = v)

      // parse query params to get context and contextId
      if (this.$route.query.contextId && this.$route.query.context
        && Object.values(ContextOrigin).includes(this.$route.query.context as ContextOrigin)) {
        this.context = {
          origin: this.$route.query.context as ContextOrigin,
          id: this.$route.query.contextId as string,
        }
        this.series.context = this.context
      }

      await this.loadPage(seriesId, this.page, this.sortActive)
    },
    parseQuerySortOrDefault(querySort: any): SortActive {
      return parseQuerySort(querySort, this.sortOptions) || this.$_.clone(this.sortDefault)
    },
    parseQueryFilterStatus(queryStatus: any): string[] {
      return queryStatus ? queryStatus.toString().split(',').filter((x: string) => Object.keys(ReadStatus).includes(x)) : []
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {seriesId: this.$route.params.seriesId},
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
        },
      } as Location
      mergeFilterParams(this.filters, loc.query)
      loc.query['filterMode'] = this.validateFiltersMode(this.filtersMode)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    async loadPage(seriesId: string, page: number, sort: SortActive) {
      this.selectedBooks = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      const conditions = [] as SearchConditionBook[]
      conditions.push(new SearchConditionSeriesId(new SearchOperatorIs(seriesId)))
      if (this.filters.readStatus && this.filters.readStatus.length > 0) conditions.push(new SearchConditionAnyOfBook(this.filters.readStatus))
      if (this.filters.tag && this.filters.tag.length > 0) this.filtersMode?.tag?.allOf ? conditions.push(new SearchConditionAllOfBook(this.filters.tag)) : conditions.push(new SearchConditionAnyOfBook(this.filters.tag))
      if (this.filters.mediaProfile && this.filters.mediaProfile.length > 0) this.filtersMode?.mediaProfile?.allOf ? conditions.push(new SearchConditionAllOfBook(this.filters.mediaProfile)) : conditions.push(new SearchConditionAnyOfBook(this.filters.mediaProfile))
      authorRoles.forEach((role: string) => {
        if (role in this.filters) {
          const authorConditions = this.filters[role].map((name: string) => {
            if (name === FILTER_ANY)
              return new SearchConditionAuthor(new SearchOperatorIs({
                role: role,
              }))
            else if (name === FILTER_NONE)
              return new SearchConditionAuthor(new SearchOperatorIsNot({
                role: role,
              }))
            else
              return new SearchConditionAuthor(new SearchOperatorIs({
                name: name,
                role: role,
              }))
          })
          conditions.push(this.filtersMode[role]?.allOf ? new SearchConditionAllOfBook(authorConditions) : new SearchConditionAnyOfBook(authorConditions))
        }
      })

      const booksPage = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAllOfBook(conditions),
      }, pageRequest)

      this.totalPages = booksPage.totalPages
      this.totalElements = booksPage.totalElements
      this.books = booksPage.content
    },
    analyze() {
      this.$komgaSeries.analyzeSeries(this.series)
    },
    refreshMetadata() {
      this.$komgaSeries.refreshMetadata(this.series)
    },
    editSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.series)
    },
    editSingleBook(book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    bulkEditMultipleBooks() {
      this.$store.dispatch('dialogUpdateBulkBooks', this.$_.sortBy(this.selectedBooks, ['metadata.numberSort']))
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks.map(b => b.id))
    },
    async markSelectedRead() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true}),
      ))
      this.selectedBooks = []
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = []
    },
    deleteBooks() {
      this.$store.dispatch('dialogDeleteBook', this.selectedBooks)
    },
    removeFromCollection(collectionId: string) {
      const col = this.collections.find(x => x.id == collectionId)
      const modified = Object.assign({}, {seriesIds: col?.seriesIds.filter(x => x != this.seriesId)})
      if (modified!.seriesIds!.length == 0)
        this.$komgaCollections.deleteCollection(col!.id)
      else
        this.$komgaCollections.patchCollection(col!.id, modified)
    },
  },
})
</script>

<style scoped>
</style>
