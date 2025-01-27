<template>
  <v-expansion-panels v-model="collectionPanel">
    <v-expansion-panel v-for="(c, index) in collections"
                       :key="index"
    >
      <v-expansion-panel-header>{{ $t('collections_expansion_panel.title', {name: c.name}) }}</v-expansion-panel-header>
      <v-expansion-panel-content>
        <horizontal-scroller
          :tick="collectionsLoaders[index].tick"
          @scroll-changed="(percent) => scrollChanged(collectionsLoaders[index], percent)"
        >
          <template v-slot:prepend>
            <slot name="prepend" v-bind:collection="c"/>
            <router-link class="text-overline"
                         :to="{name: 'browse-collection', params: {collectionId: c.id}}"
            >{{ $t('collections_expansion_panel.manage_collection') }}
            </router-link>
          </template>
          <template v-slot:content>
            <item-browser :items="collectionsLoaders[index].items"
                          nowrap
                          :selectable="false"
                          :action-menu="false"
                          :fixed-item-width="100"
            />
          </template>
        </horizontal-scroller>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import Vue from 'vue'
import {ContextOrigin} from '@/types/context'
import {SeriesDto} from '@/types/komga-series'
import {PageLoader} from '@/types/pageLoader'

export default Vue.extend({
  name: 'CollectionsExpansionPanels',
  components: {
    HorizontalScroller,
    ItemBrowser,
  },
  props: {
    collections: {
      type: Array as () => CollectionDto[],
      required: true,
    },
  },
  data: () => {
    return {
      collectionPanel: undefined as number | undefined,
      collectionsLoaders: [] as PageLoader<SeriesDto>[],
    }
  },
  watch: {
    collections: {
      handler(val: CollectionDto[]) {
        this.collectionPanel = undefined
        this.collectionsLoaders = val.map(coll => new PageLoader<SeriesDto>(
          {},
          (pageable: PageRequest) => this.$komgaCollections.getSeries(coll.id, pageable),
          (x: SeriesDto) => {
            x.context = {origin: ContextOrigin.COLLECTION, id: coll.id}
            return x
          },
        ))
      },
      immediate: true,
    },
    async collectionPanel(val) {
      if (val !== undefined) {
        if (!this.collectionsLoaders[val].hasLoadedAny) {
          this.collectionsLoaders[val].loadNext()
        }
      }
    },
  },
  methods: {
    async scrollChanged(loader: PageLoader<any>, percent: number) {
      if (percent > 0.95) await loader.loadNext()
    },
  },
})
</script>
