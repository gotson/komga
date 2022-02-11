<template>
  <div v-if="readList">
    <toolbar-sticky v-if="!editElements && selectedBooks.length === 0">

      <read-list-actions-menu v-if="readList"
                              :read-list="readList"
      />

      <v-toolbar-title v-if="readList">
        <span>{{ readList.name }}</span>
        <v-chip label class="mx-4">
          <span style="font-size: 1.1rem">{{ readList.bookIds.length }}</span>
        </v-chip>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="startEditElements" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-edit</v-icon>
          </template>
          <span>{{ $t('browse_readlist.edit_elements') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="editReadList" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>{{ $t('browse_readlist.edit_readlist') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="filterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
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
      @edit="editMultipleBooks"
      @bulk-edit="bulkEditMultipleBooks"
    />

    <!--  Edit elements sticky bar  -->
    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="editElements" :elevation="5" color="base">
        <v-btn icon @click="cancelEditElements">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-btn icon color="primary" @click="doEditElements" :disabled="books.length === 0">
          <v-icon>mdi-check</v-icon>
        </v-btn>

      </toolbar-sticky>
    </v-scroll-y-transition>

    <filter-drawer
      v-model="drawer"
      :clear-button="filterActive"
      @clear="resetFilters"
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
        />
      </template>
    </filter-drawer>

    <v-container fluid>

      <v-row v-if="readList.summary" class="px-2">
        <v-col>
          <read-more>{{ readList.summary }}</read-more>
        </v-col>
      </v-row>

      <v-row class="px-2">
        <v-col>
          <v-btn :title="$t('menu.download_readlist')"
                 small
                 :disabled="!canDownload"
                 :href="fileUrl">
            <v-icon left small>mdi-file-download</v-icon>
            {{ $t('common.download') }}
          </v-btn>
        </v-col>
      </v-row>

      <v-divider class="my-3"/>

      <item-browser
        :items.sync="books"
        :item-context="[ItemContext.SHOW_SERIES]"
        :selected.sync="selectedBooks"
        :edit-function="editSingleBook"
        :draggable="editElements"
        :deletable="editElements"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import {
  BOOK_CHANGED,
  BOOK_DELETED,
  READLIST_CHANGED,
  READLIST_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
} from '@/types/events'
import Vue from 'vue'
import ReadListActionsMenu from '@/components/menus/ReadListActionsMenu.vue'
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import {AuthorDto, BookDto, ReadProgressUpdateDto} from '@/types/komga-books'
import {ContextOrigin} from '@/types/context'
import {BookSseDto, ReadListSseDto, ReadProgressSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import ReadMore from '@/components/ReadMore.vue'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {ReadStatus, replaceCompositeReadStatus} from '@/types/enum-books'
import {authorRoles} from '@/types/author-roles'
import {LibraryDto} from '@/types/komga-libraries'
import {mergeFilterParams, toNameValue} from '@/functions/filter'
import {Location} from 'vue-router'
import {readListFileUrl} from '@/functions/urls'
import {ItemContext} from '@/types/items'

export default Vue.extend({
  name: 'BrowseReadList',
  components: {
    ToolbarSticky,
    ItemBrowser,
    ReadListActionsMenu,
    MultiSelectBar,
    FilterDrawer,
    FilterPanels,
    FilterList,
    ReadMore,
  },
  data: () => {
    return {
      ItemContext,
      readList: undefined as ReadListDto | undefined,
      books: [] as BookDto[],
      booksCopy: [] as BookDto[],
      selectedBooks: [] as BookDto[],
      editElements: false,
      filters: {} as FiltersActive,
      filterUnwatch: null as any,
      drawer: false,
      filterOptions: {
        library: [] as NameValue[],
        tag: [] as NameValue[],
      },
    }
  },
  props: {
    readListId: {
      type: String,
      required: true,
    },
  },
  created() {
    this.$eventHub.$on(READLIST_CHANGED, this.readListChanged)
    this.$eventHub.$on(READLIST_DELETED, this.readListDeleted)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.readProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(READLIST_CHANGED, this.readListChanged)
    this.$eventHub.$off(READLIST_DELETED, this.readListDeleted)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.readProgressChanged)
  },
  async mounted() {
    await this.resetParams(this.$route, this.readListId)

    this.loadReadList(this.readListId)

    this.setWatches()
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.readListId !== from.params.readListId) {
      this.unsetWatches()

      // reset
      await this.resetParams(this.$route, this.readListId)
      this.books = []
      this.editElements = false

      this.loadReadList(to.params.readListId)

      this.setWatches()
    }

    next()
  },
  computed: {
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {name: this.$i18n.t('filter.unread').toString(), value: ReadStatus.UNREAD_AND_IN_PROGRESS},
            {name: this.$t('filter.in_progress').toString(), value: ReadStatus.IN_PROGRESS},
            {name: this.$t('filter.read').toString(), value: ReadStatus.READ},
          ],
        },
      } as FiltersOptions
    },
    filterOptionsPanel(): FiltersOptions {
      const r = {
        library: {name: this.$t('filter.library').toString(), values: this.filterOptions.library},
        tag: {name: this.$t('filter.tag').toString(), values: this.filterOptions.tag},
      } as FiltersOptions
      authorRoles.forEach((role: string) => {
        r[role] = {
          name: this.$t(`author_roles.${role}`).toString(),
          search: async search => {
            return (await this.$komgaReferential.getAuthors(search, role, undefined, undefined, undefined, this.readListId))
              .content
              .map(x => x.name)
          },
        }
      })
      return r
    },
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    canDownload(): boolean {
      return this.$store.getters.meFileDownload
    },
    fileUrl(): string {
      return readListFileUrl(this.readListId)
    },
    filterActive(): boolean {
      return Object.keys(this.filters).some(x => this.filters[x].length !== 0)
    },
  },
  methods: {
    resetFilters() {
      this.drawer = false
      for (const prop in this.filters) {
        this.$set(this.filters, prop, [])
      }
      this.$store.commit('setReadListFilter', {id: this.readListId, filter: this.filters})
      this.updateRouteAndReload()
    },
    async resetParams(route: any, readListId: string) {
      // load dynamic filters
      this.$set(this.filterOptions, 'library', this.$store.state.komgaLibraries.libraries.map((x: LibraryDto) => ({
        name: x.name,
        value: x.id,
      })))

      const tags = await this.$komgaReferential.getBookTags(undefined, readListId)
      this.$set(this.filterOptions, 'tag', toNameValue(tags))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.readStatus || route.query.tag || route.query.library || authorRoles.some(role => role in route.query)) {
        activeFilters = {
          readStatus: route.query.readStatus || [],
          library: route.query.library || [],
          tag: route.query.tag || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
        })
      } else {
        activeFilters = this.$store.getters.getReadListFilter(route.params.readListId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        readStatus: filters.readStatus?.filter(x => Object.keys(ReadStatus).includes(x)) || [],
        library: filters.library?.filter(x => this.filterOptions.library.map(n => n.value).includes(x)) || [],
        tag: filters.tag?.filter(x => this.filterOptions.tag.map(n => n.value).includes(x)) || [],
      } as any
      authorRoles.forEach((role: string) => {
        validFilter[role] = filters[role] || []
      })
      return validFilter
    },
    setWatches() {
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$store.commit('setReadListFilter', {id: this.readListId, filter: val})
        this.updateRouteAndReload()
      })
    },
    unsetWatches() {
      this.filterUnwatch()
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.updateRoute()
      this.loadBooks(this.readListId)

      this.setWatches()
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {readListId: this.$route.params.readListId},
        query: {},
      } as Location
      mergeFilterParams(this.filters, loc.query)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    readListChanged(event: ReadListSseDto) {
      if (event.readListId === this.readListId) {
        this.loadReadList(this.readListId)
      }
    },
    readListDeleted(event: ReadListSseDto) {
      if (event.readListId === this.readListId) {
        this.$router.push({name: 'browse-readlists', params: {libraryId: 'all'}})
      }
    },
    async loadReadList(readListId: string) {
      this.$komgaReadLists.getOneReadList(readListId)
        .then(v => this.readList = v)
      await this.loadBooks(readListId)
    },
    async loadBooks(readListId: string) {
      let authorsFilter = [] as AuthorDto[]
      authorRoles.forEach((role: string) => {
        if (role in this.filters) this.filters[role].forEach((name: string) => authorsFilter.push({
          name: name,
          role: role,
        }))
      })

      this.books = (await this.$komgaReadLists.getBooks(readListId, {unpaged: true} as PageRequest, this.filters.library, replaceCompositeReadStatus(this.filters.readStatus), this.filters.tag, authorsFilter)).content
      this.books.forEach((x: BookDto) => x.context = {origin: ContextOrigin.READLIST, id: readListId})
      this.booksCopy = [...this.books]
      this.selectedBooks = []
    },
    reloadBooks: throttle(function (this: any) {
      this.loadBooks(this.readListId)
    }, 1000),
    editSingleBook(book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    bulkEditMultipleBooks() {
      this.$store.dispatch('dialogUpdateBulkBooks', this.selectedBooks)
    },
    async markSelectedRead() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true} as ReadProgressUpdateDto),
      ))
      this.selectedBooks = []
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = []
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks)
    },
    startEditElements() {
      this.filters = {}
      this.editElements = true
    },
    cancelEditElements() {
      this.editElements = false
      this.books = [...this.booksCopy]
    },
    doEditElements() {
      this.editElements = false
      const update = {
        bookIds: this.books.map(x => x.id),
      } as ReadListUpdateDto
      this.$komgaReadLists.patchReadList(this.readListId, update)
    },
    editReadList() {
      this.$store.dispatch('dialogEditReadList', this.readList)
    },
    bookChanged(event: BookSseDto) {
      if (this.books.some(b => b.id === event.bookId)) this.reloadBooks()
    },
    readProgressChanged(event: ReadProgressSseDto) {
      if (this.books.some(b => b.id === event.bookId)) this.reloadBooks()
    },
  },
})
</script>
