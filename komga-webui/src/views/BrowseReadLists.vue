<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedReadLists.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>
      <libraries-actions-menu v-else/>

      <v-toolbar-title>
        <span>{{ library ? library.name : $t('common.all_libraries') }}</span>
        <v-chip label class="mx-4" v-if="totalElements">
          <span style="font-size: 1.1rem">{{ totalElements }}</span>
        </v-chip>
      </v-toolbar-title>

      <v-spacer/>

      <library-navigation v-if="$vuetify.breakpoint.mdAndUp" :libraryId="libraryId"/>

      <v-spacer/>

      <page-size-select v-model="pageSize"/>
    </toolbar-sticky>

    <multi-select-bar
      v-model="selectedReadLists"
      kind="readlists"
      show-select-all
      @unselect-all="selectedReadLists = []"
      @select-all="selectedReadLists = readLists"
      @delete="deleteReadLists"
    />

    <library-navigation v-if="$vuetify.breakpoint.smAndDown" :libraryId="libraryId" bottom-navigation/>

    <v-container fluid>
      <v-pagination
        v-if="totalPages > 1"
        v-model="page"
        :total-visible="paginationVisible"
        :length="totalPages"
      />

      <item-browser
        :items="readLists"
        :selectable="isAdmin"
        :selected.sync="selectedReadLists"
        :edit-function="isAdmin ? editSingle : undefined"
      />

      <v-pagination
        v-if="totalPages > 1"
        v-model="page"
        :total-visible="paginationVisible"
        :length="totalPages"
      />
    </v-container>

  </div>
</template>

<script lang="ts">
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {LIBRARY_CHANGED, READLIST_ADDED, READLIST_CHANGED, READLIST_DELETED} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'
import {LibrarySseDto} from '@/types/komga-sse'
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import {LibraryDto} from '@/types/komga-libraries'
import {ReadListDto} from '@/types/komga-readlists'
import LibrariesActionsMenu from '@/components/menus/LibrariesActionsMenu.vue'

export default Vue.extend({
  name: 'BrowseReadLists',
  components: {
    LibrariesActionsMenu,
    LibraryActionsMenu,
    ToolbarSticky,
    LibraryNavigation,
    ItemBrowser,
    PageSizeSelect,
    MultiSelectBar,
  },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      readLists: [] as ReadListDto[],
      selectedReadLists: [] as ReadListDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
    }
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  created() {
    this.$eventHub.$on(READLIST_ADDED, this.reloadElements)
    this.$eventHub.$on(READLIST_CHANGED, this.reloadElements)
    this.$eventHub.$on(READLIST_DELETED, this.reloadElements)
    this.$eventHub.$on(LIBRARY_CHANGED, this.reloadLibrary)
  },
  beforeDestroy() {
    this.$eventHub.$off(READLIST_ADDED, this.reloadElements)
    this.$eventHub.$off(READLIST_CHANGED, this.reloadElements)
    this.$eventHub.$off(READLIST_DELETED, this.reloadElements)
    this.$eventHub.$off(LIBRARY_CHANGED, this.reloadLibrary)
  },
  mounted() {
    this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.READLISTS})
    this.pageSize = this.$store.state.persistedState.browsingPageSize || this.pageSize

    // restore from query param
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadLibrary(this.libraryId)

    this.setWatches()
  },
  beforeRouteUpdate(to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      // reset
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.readLists = []

      this.loadLibrary(to.params.libraryId)
    }

    next()
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
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
  },
  methods: {
    setWatches() {
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val)
      })
    },
    unsetWatches() {
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.page = 1

      this.updateRoute()
      this.loadPage(this.libraryId, this.page)

      this.setWatches()
    },
    updateRoute() {
      this.$router.replace({
        name: this.$route.name,
        params: {libraryId: this.$route.params.libraryId},
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
        },
      } as Location).catch((_: any) => {
      })
    },
    reloadElements() {
      this.loadLibrary(this.libraryId)
    },
    reloadLibrary(event: LibrarySseDto) {
      if (event.libraryId === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    async loadLibrary(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)
      if (this.library != undefined) document.title = `Komga - ${this.library.name}`
      await this.loadPage(libraryId, this.page)

      if (this.totalElements === 0) {
        await this.$router.push({name: 'browse-libraries', params: {libraryId: libraryId.toString()}})
      }
    },
    async loadPage(libraryId: string, page: number) {
      this.selectedReadLists = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      const lib = libraryId !== LIBRARIES_ALL ? [libraryId] : undefined
      const elementsPage = await this.$komgaReadLists.getReadLists(lib, pageRequest)

      this.totalPages = elementsPage.totalPages
      this.totalElements = elementsPage.totalElements
      this.readLists = elementsPage.content
    },
    getLibraryLazy(libraryId: string): LibraryDto | undefined {
      if (libraryId !== LIBRARIES_ALL) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
    },
    editSingle(element: ReadListDto) {
      this.$store.dispatch('dialogEditReadList', element)
    },
    deleteReadLists () {
      this.$store.dispatch('dialogDeleteReadList', this.selectedReadLists)
    },
  },
})
</script>
