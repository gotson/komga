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
      <v-menu offset-y>
        <template v-slot:activator="{on}">
          <v-btn icon v-on="on">
            <v-icon :color="$_.isEmpty(filterStatus) ? null : 'secondary'"
            >mdi-filter-variant
            </v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-subheader>STATUS</v-subheader>
          <v-list-item v-for="s in SeriesStatus"
                       :key="s"
          >
            <v-list-item-title class="text-capitalize">
              <v-checkbox v-model="filterStatus"
                          :label="s.toString().toLowerCase()"
                          color="secondary"
                          class="mt-1 ml-2"
                          :value="s"/>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

      <!--   Sort menu   -->
      <sort-menu-button :sort-default="sortDefault"
                        :sort-options="sortOptions"
                        :sort-active.sync="sortActive"
      />
    </toolbar-sticky>

    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="selected.length > 0" :elevation="5" color="white">
        <v-btn icon @click="selected=[]">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>
          <span>{{ selected.length }} selected</span>
        </v-toolbar-title>

        <v-spacer/>

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

    <item-browser :items="series" :selected.sync="selected" class="px-6" :edit-function="this.singleEdit">
      <template v-slot:item="{ data }">
        <card-series :series="data.item"
                     :width="data.itemWidth"
                     :selected="data.active"
                     :select="data.toggle"
                     :preSelect="data.preselect"
                     :edit="data.editItem"
        />
      </template>
      <template #empty v-if="filterStatus.length > 0">
        <empty-state title="The active filter has no matches"
                     sub-title="Use the menu above to change the active filter"
                     icon="mdi-book-multiple"
                     icon-color="secondary"
        >
          <v-btn @click="filterStatus = []">Clear filter</v-btn>
        </empty-state>
      </template>
      <template #empty v-else>
        <empty-state title="There are no current libraries"
                     sub-title="Use the + button on the side bar to add a library"
                     icon="mdi-book-multiple"
                     icon-color="secondary"
        >
        </empty-state>
      </template>
    </item-browser>
  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import CardSeries from '@/components/CardSeries.vue'
import EmptyState from '@/components/EmptyState.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import LibraryActionsMenu from '@/components/LibraryActionsMenu.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { parseQuerySort } from '@/functions/query-params'
import VisibleElements from '@/mixins/VisibleElements'
import { LoadState } from '@/types/common'
import mixins from 'vue-typed-mixins'
import { SeriesStatus } from '@/types/enum-series'
import ItemBrowser from '@/components/ItemBrowser.vue'

export default mixins(VisibleElements).extend({
  name: 'BrowseLibraries',
  components: { LibraryActionsMenu, CardSeries, EmptyState, ToolbarSticky, SortMenuButton, Badge, EditSeriesDialog, ItemBrowser },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      series: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editSeriesSingle: {} as SeriesDto,
      pagesState: [] as LoadState[],
      pageSize: 20,
      totalElements: null as number | null,
      sortOptions: [
        { name: 'Name', key: 'metadata.titleSort' },
        { name: 'Date added', key: 'createdDate' },
        { name: 'Date updated', key: 'lastModifiedDate' },
      ] as SortOption[],
      sortActive: {} as SortActive,
      sortDefault: { key: 'metadata.titleSort', order: 'asc' } as SortActive,
      filterStatus: [] as string[],
      SeriesStatus,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
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
    async visibleElements (val) {
      for (const i of val) {
        const pageNumber = Math.floor(i / this.pageSize)
        if (this.pagesState[pageNumber] === undefined || this.pagesState[pageNumber] === LoadState.NotLoaded) {
          this.processPage(await this.loadPage(pageNumber, this.libraryId))
        }
      }

      const max = this.$_.max(val) as number | undefined
      const index = (max === undefined ? 0 : max).toString()

      if (this.$route.params.index !== index) {
        this.updateRoute(index)
      }
    },
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
  async created () {
    this.library = await this.getLibraryLazy(this.libraryId)
  },
  mounted () {
    // fill series skeletons if an index is provided, so scroll position can be restored
    if (this.$route.params.index) {
      this.series = Array(Number(this.$route.params.index)).fill(null)
    } else { // else fill one page of skeletons
      this.series = Array(this.pageSize).fill(null)
    }

    // restore sort from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)

    // restore filter status from query params
    this.filterStatus = this.parseQueryFilterStatus(this.$route.query.status)

    this.reloadData(Number(this.$route.params.libraryId), this.series.length)

    this.setWatches()
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.unsetWatches()

      this.library = this.getLibraryLazy(Number(to.params.libraryId))

      if (to.params.index) {
        this.series = Array(Number(to.params.index)).fill(null)
      } else { // else fill one page of skeletons
        this.series = Array(this.pageSize).fill(null)
      }
      this.sortActive = this.parseQuerySortOrDefault(to.query.sort)
      this.filterStatus = this.parseQueryFilterStatus(to.query.status)
      this.reloadData(Number(to.params.libraryId), this.series.length)

      this.setWatches()
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    setWatches () {
      this.sortUnwatch = this.$watch('sortActive', this.updateRouteAndReload)
      this.filterUnwatch = this.$watch('filterStatus', this.updateRouteAndReload)
    },
    unsetWatches () {
      this.sortUnwatch()
      this.filterUnwatch()
    },
    updateRouteAndReload () {
      this.updateRoute()
      this.reloadData(this.libraryId)
    },
    parseQuerySortOrDefault (querySort: any): SortActive {
      return parseQuerySort(querySort, this.sortOptions) || this.$_.clone(this.sortDefault)
    },
    parseQueryFilterStatus (queryStatus: any): string[] {
      return queryStatus ? queryStatus.toString().split(',').filter((x: string) => Object.keys(SeriesStatus).includes(x)) : []
    },
    reloadData (libraryId: number, countItem?: number) {
      this.totalElements = null
      this.pagesState = []
      this.visibleElements = []
      this.series = Array(countItem || this.pageSize).fill(null)
      this.loadInitialData(libraryId)
    },
    updateRoute (index?: string) {
      this.$router.replace({
        name: this.$route.name,
        params: { libraryId: this.$route.params.libraryId, index: index || this.$route.params.index },
        query: {
          sort: `${this.sortActive.key},${this.sortActive.order}`,
          status: `${this.filterStatus}`,
        },
      }).catch(_ => {
      })
    },
    async loadInitialData (libraryId: number, pageToLoad: number = 0) {
      this.processPage(await this.loadPage(pageToLoad, libraryId))
    },
    async loadPage (page: number, libraryId: number): Promise<Page<SeriesDto>> {
      this.pagesState[page] = LoadState.Loading
      const pageRequest = {
        page: page,
        size: this.pageSize,
      } as PageRequest

      if (this.sortActive != null) {
        pageRequest.sort = [`${this.sortActive.key},${this.sortActive.order}`]
      }

      let requestLibraryId
      if (libraryId !== 0) {
        requestLibraryId = libraryId
      }
      return this.$komgaSeries.getSeries(requestLibraryId, pageRequest, undefined, this.filterStatus)
    },
    processPage (page: Page<SeriesDto>) {
      if (this.totalElements === null) {
        // initialize page data
        this.totalElements = page.totalElements
        this.series = Array(this.totalElements).fill(null)
        this.pagesState = Array(page.totalPages).fill(LoadState.NotLoaded)
      }
      this.series.splice(page.number * page.size, page.size, ...page.content)
      this.pagesState[page.number] = LoadState.Loaded
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
  },
})
</script>
<style scoped>
</style>
