<template>
  <v-expansion-panels v-model="readListPanel">
    <v-expansion-panel v-for="(r, index) in readLists"
                       :key="index"
    >
      <v-expansion-panel-header>{{ $t('readlists_expansion_panel.title', {name: r.name}) }}</v-expansion-panel-header>
      <v-expansion-panel-content>
        <horizontal-scroller>
          <template v-slot:prepend>
            <router-link class="text-overline"
                         :to="{name: 'browse-readlist', params: {readListId: r.id}}"
            >{{ $t('readlists_expansion_panel.manage_readlist') }}</router-link>
          </template>
          <template v-slot:content>
            <item-browser :items="readListsContent[index]"
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
      readListsContent: [[]] as any[],
    }
  },
  watch: {
    readLists: {
      handler (val) {
        this.readListPanel = undefined
        this.readListsContent = [...Array(val.length)].map(elem => new Array(0))
      },
      immediate: true,
    },
    async readListPanel (val) {
      if (val !== undefined) {
        const rlId = this.readLists[val].id
        if (this.$_.isEmpty(this.readListsContent[val])) {
          const content = (await this.$komgaReadLists.getBooks(rlId, { unpaged: true } as PageRequest)).content
          content.forEach((x: BookDto) => x.context = { origin: ContextOrigin.READLIST, id: rlId })
          this.readListsContent.splice(val, 1, content)
        }
      }
    },
  },
})
</script>
