<template>
  <div>
    <toolbar-sticky v-if="selected.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : 'All libraries' }}</span>
        <badge class="ml-4" v-if="totalElements">{{ totalElements }}</badge>
      </v-toolbar-title>

      <v-spacer/>

      <!--   Filter menu   -->
      <filter-menu-button :filters-options="filterOptions"
                          :filters-active.sync="filters"
      />

      <!--   Sort menu   -->
      <sort-menu-button :sort-default="sortDefault"
                        :sort-options="sortOptions"
                        :sort-active.sync="sortActive"
      />

      <page-size-select v-model="pageSize"/>

    </toolbar-sticky>

    <!--  Selection sticky bar  -->
    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="selected.length > 0" :elevation="5" color="white">
        <v-btn icon @click="selected=[]">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>
          <span>{{ selected.length }} selected</span>
        </v-toolbar-title>

        <v-spacer/>

        <v-btn icon @click="markSelectedRead()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-bookmark-check</v-icon>
            </template>
            <span>Mark as Read</span>
          </v-tooltip>
        </v-btn>

        <v-btn icon @click="markSelectedUnread()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-bookmark-remove</v-icon>
            </template>
            <span>Mark as Unread</span>
          </v-tooltip>
        </v-btn>

        <v-btn icon @click="dialogEdit = true" v-if="isAdmin">
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
      </toolbar-sticky>
    </v-scroll-y-transition>

    <edit-series-dialog v-model="dialogEdit"
                        :series.sync="selectedSeries"
    />

    <edit-series-dialog v-model="dialogEditSingle"
                        :series.sync="editSeriesSingle"
    />

    <v-container fluid class="px-6">
      <empty-state
        v-if="totalPages === 0"
        title="The active filter has no matches"
        sub-title="Use the menu above to change the active filter"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
      </empty-state>

      <template v-else>
        <v-pagination
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser
          :items="series"
          :selected.sync="selected"
          :edit-function="this.singleEdit"
          class="px-4"
        />
      </template>
    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterMenuButton from '@/components/FilterMenuButton.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import LibraryActionsMenu from '@/components/LibraryActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { parseQuerySort } from '@/functions/query-params'
import { SeriesStatus } from '@/types/enum-series'
import Vue from 'vue'

const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: {
    LibraryActionsMenu,
    EmptyState,
    ToolbarSticky,
    SortMenuButton,
    FilterMenuButton,
    Badge,
    EditSeriesDialog,
    ItemBrowser,
    PageSizeSelect,
  },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      series: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editSeriesSingle: {} as SeriesDto,
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortOptions: [
        { name: 'Name', key: 'metadata.titleSort' },
        { name: 'Date added', key: 'createdDate' },
        { name: 'Date updated', key: 'lastModifiedDate' },
      ] as SortOption[],
      sortActive: {} as SortActive,
      sortDefault: { key: 'metadata.titleSort', order: 'asc' } as SortActive,
      filterOptions: [{ name: 'STATUS', values: SeriesStatus }],
      filters: [[]] as any[],
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      selected: [],
      dialogEdit: false,
      dialogEditSingle: false,
    }
  },
  props: {
    libraryId: {
      type: Number,
      default: 0,
    },
  },
  watch: {
    selected (val: number[]) {
      this.selectedSeries = val.map(id => this.series.find(s => s.id === id))
        .filter(x => x !== undefined) as SeriesDto[]
    },
    selectedSeries (val: SeriesDto[]) {
      val.forEach(s => {
        const index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
      })
    },
    editSeriesSingle (val: SeriesDto) {
      const index = this.series.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.series.splice(index, 1, val)
      }
    },
  },
  mounted () {
    if (this.$cookies.isKey(cookiePageSize)) {
      this.pageSize = Number(this.$cookies.get(cookiePageSize))
    }

    // restore from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)
    this.filters.splice(0, 1, this.parseQueryFilterStatus(this.$route.query.status))
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadLibrary(this.libraryId)

    this.setWatches()
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.unsetWatches()

      // reset
      this.sortActive = this.parseQuerySortOrDefault(to.query.sort)
      this.filters.splice(0, 1, this.parseQueryFilterStatus(to.query.status))
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.series = []

      this.loadLibrary(Number(to.params.libraryId))

      this.setWatches()
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    paginationVisible (): number {
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
  },
  methods: {
    setWatches () {
      this.sortUnwatch = this.$watch('sortActive', this.updateRouteAndReload)
      this.filterUnwatch = this.$watch('filters', this.updateRouteAndReload)
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$cookies.set(cookiePageSize, val, Infinity)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val, this.sortActive)
      })
    },
    unsetWatches () {
      this.sortUnwatch()
      this.filterUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload () {
      this.unsetWatches()

      this.selected = []
      this.page = 1

      this.updateRoute()
      this.loadPage(this.libraryId, this.page, this.sortActive)

      this.setWatches()
    },
    async loadLibrary (libraryId: number) {
      this.library = this.getLibraryLazy(libraryId)
      await this.loadPage(libraryId, this.page, this.sortActive)
    },
    parseQuerySortOrDefault (querySort: any): SortActive {
      return parseQuerySort(querySort, this.sortOptions) || this.$_.clone(this.sortDefault)
    },
    parseQueryFilterStatus (queryStatus: any): string[] {
      return queryStatus ? queryStatus.toString().split(',').filter((x: string) => Object.keys(SeriesStatus).includes(x)) : []
    },
    updateRoute () {
      this.$router.replace({
        name: this.$route.name,
        params: { libraryId: this.$route.params.libraryId },
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
          status: `${this.filters[0]}`,
        },
      }).catch(_ => {
      })
    },
    async loadPage (libraryId: number, page: number, sort: SortActive) {
      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      let requestLibraryId
      if (libraryId !== 0) {
        requestLibraryId = libraryId
      }
      const seriesPage = await this.$komgaSeries.getSeries(requestLibraryId, pageRequest, undefined, this.filters[0])

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content
    },
    getLibraryLazy (libraryId: any): LibraryDto | undefined {
      if (libraryId !== 0) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
    },
    singleEdit (series: SeriesDto) {
      this.editSeriesSingle = series
      this.dialogEditSingle = true
    },
    async markSelectedRead () {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsRead(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsUnread(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
  },
})
</script>
<style scoped>
</style>
