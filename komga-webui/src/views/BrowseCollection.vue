<template>
  <div v-if="collection">
    <toolbar-sticky v-if="!editElements && selected.length === 0">

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

        <v-btn icon @click="addToCollection()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-playlist-plus</v-icon>
            </template>
            <span>Add to collection</span>
          </v-tooltip>
        </v-btn>

        <v-btn icon @click="dialogEdit = true" v-if="isAdmin">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-pencil</v-icon>
            </template>
            <span>Edit metadata</span>
          </v-tooltip>
        </v-btn>
      </toolbar-sticky>
    </v-scroll-y-transition>

    <edit-series-dialog v-model="dialogEdit"
                        :series.sync="selectedSeries"
    />

    <edit-series-dialog v-model="dialogEditSingle"
                        :series.sync="editSeriesSingle"
    />

    <collection-edit-dialog v-model="dialogEditCollection"
                            :collection="collection"
                            @updated="loadCollection(collectionId)"
    />

    <v-container fluid>

      <item-browser
        :items.sync="series"
        :selected.sync="selected"
        :edit-function="singleEdit"
        :draggable="editElements && collection.ordered"
        :deletable="editElements"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import CollectionActionsMenu from '@/components/CollectionActionsMenu.vue'
import CollectionEditDialog from '@/components/CollectionEditDialog.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { COLLECTION_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'BrowseCollection',
  components: {
    ToolbarSticky,
    ItemBrowser,
    EditSeriesDialog,
    CollectionEditDialog,
    CollectionActionsMenu,
    Badge,
  },
  data: () => {
    return {
      collection: undefined as CollectionDto | undefined,
      series: [] as SeriesDto[],
      seriesCopy: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editSeriesSingle: {} as SeriesDto,
      selected: [],
      dialogEdit: false,
      dialogEditSingle: false,
      dialogAddToCollection: false,
      dialogEditCollection: false,
      editElements: false,
    }
  },
  props: {
    collectionId: {
      type: Number,
      required: true,
    },
  },
  watch: {
    selected (val: number[]) {
      this.selectedSeries = val.map(id => this.series.find(s => s.id === id))
        .filter(x => x !== undefined) as SeriesDto[]
    },
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
    editSeriesSingle (val: SeriesDto) {
      let index = this.series.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.series.splice(index, 1, val)
      }
      index = this.seriesCopy.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.seriesCopy.splice(index, 1, val)
      }
    },
  },
  created () {
    this.$eventHub.$on(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
  },
  beforeDestroy () {
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

      this.loadCollection(Number(to.params.collectionId))
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    async loadCollection (collectionId: number) {
      this.collection = await this.$komgaCollections.getOneCollection(collectionId)
      this.series = (await this.$komgaCollections.getSeries(collectionId, { unpaged: true } as PageRequest)).content
      this.seriesCopy = [...this.series]
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
      this.dialogEditCollection = true
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
