<template>
  <v-expansion-panels v-model="collectionPanel">
    <v-expansion-panel v-for="(c, index) in collections"
                       :key="index"
    >
      <v-expansion-panel-header>{{ $t('collections_expansion_panel.title', {name: c.name}) }}</v-expansion-panel-header>
      <v-expansion-panel-content>
        <horizontal-scroller>
          <template v-slot:prepend>
            <router-link class="text-overline"
                         :to="{name: 'browse-collection', params: {collectionId: c.id}}"
            >{{ $t('collections_expansion_panel.manage_collection') }}</router-link>
          </template>
          <template v-slot:content>
            <item-browser :items="collectionsContent[index]"
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
      collectionsContent: [[]] as any[],
    }
  },
  watch: {
    collections: {
      handler (val) {
        this.collectionPanel = undefined
        this.collectionsContent = [...Array(val.length)].map(elem => new Array(0))
      },
      immediate: true,
    },
    async collectionPanel (val) {
      if (val !== undefined) {
        const collId = this.collections[val].id
        if (this.$_.isEmpty(this.collectionsContent[val])) {
          const content = (await this.$komgaCollections.getSeries(collId, { unpaged: true } as PageRequest)).content
          this.collectionsContent.splice(val, 1, content)
        }
      }
    },
  },
})
</script>
