<template>
  <v-expansion-panels v-model="readListPanel">
    <v-expansion-panel v-for="(r, index) in readLists"
                       :key="index"
    >
      <v-expansion-panel-header>{{ $t('readlists_expansion_panel.title', {name: r.name}) }}</v-expansion-panel-header>
      <v-expansion-panel-content>
        <horizontal-scroller
          :tick="readListsLoaders[index].tick"
          @scroll-changed="(percent) => scrollChanged(readListsLoaders[index], percent)"
        >
          <template v-slot:prepend>
            <slot name="prepend" v-bind:readlist="r"/>
            <router-link class="text-overline"
                         :to="{name: 'browse-readlist', params: {readListId: r.id}}"
            >{{ $t('readlists_expansion_panel.manage_readlist') }}
            </router-link>
          </template>
          <template v-slot:content>
            <item-browser :items="readListsLoaders[index].items"
                          :item-context="[ItemContext.SHOW_SERIES]"
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
import {BookDto} from '@/types/komga-books'
import {ContextOrigin} from '@/types/context'
import {ItemContext} from '@/types/items'
import {PageLoader} from '@/types/pageLoader'
import {ReadListDto} from '@/types/komga-readlists'

export default Vue.extend({
  name: 'ReadListsExpansionPanels',
  components: {
    HorizontalScroller,
    ItemBrowser,
  },
  props: {
    readLists: {
      type: Array as () => ReadListDto[],
      required: true,
    },
  },
  data: () => {
    return {
      ItemContext,
      readListPanel: undefined as number | undefined,
      readListsLoaders: [] as PageLoader<BookDto>[],
    }
  },
  watch: {
    readLists: {
      handler(val: ReadListDto[]) {
        this.readListPanel = undefined
        this.readListsLoaders = val.map(rl => new PageLoader<BookDto>(
          {},
          (pageable: PageRequest) => this.$komgaReadLists.getBooks(rl.id, pageable),
          (x: BookDto) => {
            x.context = {origin: ContextOrigin.READLIST, id: rl.id}
            return x
          },
        ))
      },
      immediate: true,
    },
    async readListPanel(val) {
      if (val !== undefined) {
        if (!this.readListsLoaders[val].hasLoadedAny) {
          this.readListsLoaders[val].loadNext()
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
