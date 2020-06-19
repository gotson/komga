<template>
  <div :style="$vuetify.breakpoint.name === 'xs' ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky>
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : 'All libraries' }}</span>
        <badge class="ml-4">{{ collections.length }}</badge>
      </v-toolbar-title>
    </toolbar-sticky>

    <library-navigation :libraryId="libraryId"/>

    <v-container fluid class="px-6">
      <item-browser
        :items="collections"
        :selectable="false"
        class="px-4"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import LibraryActionsMenu from '@/components/LibraryActionsMenu.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import Vue from 'vue'

const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'BrowseCollections',
  components: {
    LibraryActionsMenu,
    ToolbarSticky,
    LibraryNavigation,
    ItemBrowser,
    Badge,
  },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      collections: [] as CollectionDto[],
    }
  },
  props: {
    libraryId: {
      type: Number,
      default: 0,
    },
  },
  mounted () {
    this.loadLibrary(this.libraryId)
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      // reset
      this.collections = []

      this.loadLibrary(Number(to.params.libraryId))
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    async loadLibrary (libraryId: number) {
      this.library = this.getLibraryLazy(libraryId)
      const lib = libraryId !== 0 ? [libraryId] : undefined
      this.collections = await this.$komgaCollections.getCollections(lib)
    },
    getLibraryLazy (libraryId: any): LibraryDto | undefined {
      if (libraryId !== 0) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
    },
  },
})
</script>
