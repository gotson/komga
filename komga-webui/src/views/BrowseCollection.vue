<template>
  <div v-if="collection">
    <toolbar-sticky v-if="!editElements && selectedSeries.length === 0">

      <collection-actions-menu v-if="collection"
                               :collection="collection"
      />

      <v-toolbar-title v-if="collection">
        <span>{{ collection.name }}</span>
        <badge class="mx-4">{{ collection.seriesIds.length }}</badge>
        <span v-if="collection.ordered"
              class="font-italic overline"
        >(manual ordering)</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="startEditElements" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-edit</v-icon>
          </template>
          <span>Edit elements</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="editCollection" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>Edit collection</span>
        </v-tooltip>
      </v-btn>

    </toolbar-sticky>

    <series-multi-select-bar
      v-model="selectedSeries"
      @unselect-all="selectedSeries = []"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-collection="addToCollection"
      @edit="editMultipleSeries"
    />

    <!--  Edit elements sticky bar  -->
    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="editElements" :elevation="5" color="white">
        <v-btn icon @click="cancelEditElements">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-btn icon color="primary" @click="doEditElements" :disabled="series.length === 0">
          <v-icon>mdi-check</v-icon>
        </v-btn>

      </toolbar-sticky>
    </v-scroll-y-transition>

    <v-container fluid>

      <item-browser
        :items.sync="series"
        :selected.sync="selectedSeries"
        :edit-function="editSingleSeries"
        :draggable="editElements && collection.ordered"
        :deletable="editElements"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import CollectionActionsMenu from '@/components/menus/CollectionActionsMenu.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import { COLLECTION_CHANGED, COLLECTION_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'

export default Vue.extend({
  name: 'BrowseCollection',
  components: {
    ToolbarSticky,
    ItemBrowser,
    CollectionActionsMenu,
    Badge,
    SeriesMultiSelectBar,
  },
  data: () => {
    return {
      collection: undefined as CollectionDto | undefined,
      series: [] as SeriesDto[],
      seriesCopy: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editElements: false,
    }
  },
  props: {
    collectionId: {
      type: String,
      required: true,
    },
  },
  watch: {
    selectedSeries (val: SeriesDto[]) {
      val.forEach(s => {
        let index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
        index = this.seriesCopy.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.seriesCopy.splice(index, 1, s)
        }
      })
    },
  },
  created () {
    this.$eventHub.$on(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
  },
  beforeDestroy () {
    this.$eventHub.$off(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
  },
  mounted () {
    this.loadCollection(this.collectionId)
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.collectionId !== from.params.collectionId) {
      // reset
      this.series = []
      this.editElements = false

      this.loadCollection(to.params.collectionId)
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    collectionChanged (event: EventCollectionChanged) {
      if (event.id === this.collectionId) {
        this.loadCollection(this.collectionId)
      }
    },
    async loadCollection (collectionId: string) {
      this.collection = await this.$komgaCollections.getOneCollection(collectionId)
      this.series = (await this.$komgaCollections.getSeries(collectionId, { unpaged: true } as PageRequest)).content
      this.seriesCopy = [...this.series]
    },
    editSingleSeries (series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries () {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
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
    addToCollection () {
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries)
    },
    startEditElements () {
      this.editElements = true
    },
    cancelEditElements () {
      this.editElements = false
      this.series = [...this.seriesCopy]
    },
    doEditElements () {
      this.editElements = false
      const update = {
        seriesIds: this.series.map(x => x.id),
      } as CollectionUpdateDto
      this.$komgaCollections.patchCollection(this.collectionId, update)
      this.loadCollection(this.collectionId)
    },
    editCollection () {
      this.$store.dispatch('dialogEditCollection', this.collection)
    },
    afterDelete () {
      this.$router.push({ name: 'browse-collections', params: { libraryId: '0' } })
    },
    reloadSeries (event: EventSeriesChanged) {
      if (this.series.some(s => s.id === event.id)) this.loadCollection(this.collectionId)
    },
  },
})
</script>
