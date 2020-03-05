<template>
  <div>
    <toolbar-sticky v-if="selected.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : 'All libraries' }}</span>
        <badge class="ml-4" v-if="totalElements" v-model="totalElements"/>
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

    <edit-series-dialog v-model="dialogEditSingle" :series.sync="editSeriesSingle"></edit-series-dialog>

    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="selected ? selected.length > 0: false" :elevation="5" color="white">
        <v-btn icon @click="selected = []">
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
    <multiple-select
      :selected.sync="selected"
      :items="this.series"
    >
      <template v-slot:selectedItems="{ selectedItems }">
        <edit-series-dialog v-model="dialogEdit" :series.sync="selected"></edit-series-dialog>
      </template>
      <template v-slot:item="{ pre, items }">
        <grid-cards :items="items">
          <template v-slot:card="{ item, width }" >
            <v-item v-slot:default="{ active, toggle }" :value="item">
              <div>
                <card-series
                  :series="item"
                  :width="width"
                  :selected="active"
                  :preSelect="pre"
                  :edit="singleEdit"
                  :select="toggle"
                >
                </card-series>
              </div>
            </v-item>
          </template>
        </grid-cards>
      </template>
      <template v-slot:empty>
        <!--  Empty state if filter returns no books  -->
        <v-row justify="center">
          <empty-state title="The active filter has no matches"
                       sub-title="Use the menu above to change the active filter"
                       icon="mdi-book-multiple"
                       icon-color="secondary"
          >
            <v-btn @click="filterStatus = []">Clear filter</v-btn>
          </empty-state>
        </v-row>
      </template>
    </multiple-select>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Badge from '@/components/Badge.vue'
import CardSeries from '@/components/CardSeries.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import LibraryActionsMenu from '@/components/LibraryActionsMenu.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import GridCards from '@/components/GridCards.vue'
import MultipleSelect from '@/components/MultipleSelect.vue'
import { parseQuerySort } from '@/functions/query-params'
import { LoadState, SeriesStatus } from '@/types/common'
import EmptyState from '@/components/EmptyState.vue'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: { CardSeries, MultipleSelect, EmptyState, GridCards, LibraryActionsMenu, ToolbarSticky, SortMenuButton, Badge, EditSeriesDialog },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      series: [] as SeriesDto[],
      editSeriesSingle: {} as SeriesDto,
      pagesState: [] as LoadState[],
      pageSize: 20,
      totalElements: null as number | null,
      sortOptions: [
        { name: 'Name', key: 'metadata.titleSort' },
        { name: 'Date added', key: 'createdDate' },
        { name: 'Date updated', key: 'lastModifiedDate' }
      ] as SortOption[],
      sortActive: {} as SortActive,
      sortDefault: { key: 'metadata.titleSort', order: 'asc' } as SortActive,
      filterStatus: [] as string[],
      SeriesStatus,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      selected: [],
      dialogEdit: false,
      dialogEditSingle: false
    }
  },
  props: {
    libraryId: {
      type: Number,
      default: 0
    }
  },
  watch: {
    editSeriesSingle (val: SeriesDto) {
      const index = this.series.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.series.splice(index, 1, val)
      }
    }
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
    }
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
      this.series = Array(countItem || this.pageSize).fill(null)
      this.loadInitialData(libraryId)
    },
    updateRoute (index?: string) {
      this.$router.replace({
        name: this.$route.name,
        params: { libraryId: this.$route.params.libraryId, index: index || this.$route.params.index },
        query: {
          sort: `${this.sortActive.key},${this.sortActive.order}`,
          status: `${this.filterStatus}`
        }
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
        size: this.pageSize
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
    }
  }
})
</script>
<style scoped>
</style>
