<template>
  <v-item-group multiple v-model="selectedItems">
    <div v-if="hasItems"
         ref="content"
         v-resize="onResize"
    >
      <draggable v-model="localItems"
                 :class="flexClass"
                 handle=".handle"
                 v-bind="dragOptions"
                 :forceFallback="true"
                 :scroll-sensitivity="200"
                 @start="transitions = false"
                 @end="transitions = true"
      >
        <transition-group type="transition"
                          :name="transitions ? 'flip-list' : null"
                          :class="flexClass"
        >
          <v-item
            v-for="item in localItems"
            :key="item.id"
            class="my-2 mx-2"
            v-slot:default="{ toggle, active }" :value="item"
          >
            <slot name="item">
              <div style="position: relative"
                   :class="draggable ? 'draggable-item' : undefined"
              >
                <item-card
                  class="item-card"
                  :item="item"
                  :item-context="itemContext"
                  :width="itemWidth"
                  :selected="active"
                  :no-link="draggable || deletable"
                  :preselect="shouldPreselect"
                  :onEdit="(draggable || deletable) ? undefined : editFunction"
                  :onSelected="(draggable || deletable) ? undefined : selectable ? (item, event) => handleSelectClick(toggle, item, event): undefined"
                  :action-menu="(draggable || deletable) ? false : actionMenu"
                  :disable-fab="draggable || deletable"
                ></item-card>

                <v-slide-y-reverse-transition>
                  <v-text-field v-if="draggable"
                                v-model="localItemsIndex[JSON.stringify(item)]"
                                type="number"
                                min="0"
                                :max="localItems.length - 1"
                                solo
                                style="position: absolute; top: 0; left: 0;"
                                ref=""
                                @blur="updateIndex(item)"
                                @keydown.enter="updateIndex(item)"
                  />
                </v-slide-y-reverse-transition>

                <v-slide-y-reverse-transition>
                  <v-icon v-if="draggable"
                          class="handle"
                          style="position: absolute; bottom: 0; left: 50%; margin-left: -12px;"
                  >
                    mdi-drag-horizontal
                  </v-icon>
                </v-slide-y-reverse-transition>

                <!-- FAB delete (center) -->
                <v-fab-transition>
                  <v-btn
                    v-if="deletable"
                    fab
                    small
                    color="accent"
                    class="fab-delete"
                    @click="deleteItem(item)"
                    style="position: absolute; bottom: 10px; right: 10px;"
                  >
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </v-fab-transition>

              </div>
            </slot>
          </v-item>
        </transition-group>
      </draggable>
    </div>
    <v-row v-else justify="center">
      <slot name="empty"></slot>
    </v-row>
  </v-item-group>
</template>

<script lang="ts">
import ItemCard from '@/components/ItemCard.vue'
import {computeCardWidth} from '@/functions/grid-utilities'
import Vue from 'vue'
import draggable from 'vuedraggable'
import {ItemContext} from '@/types/items'

export default Vue.extend({
  name: 'ItemBrowser',
  components: {ItemCard, draggable},
  props: {
    items: {
      type: Array,
      required: true,
    },
    itemContext: {
      type: Array as () => ItemContext[],
      default: () => [],
    },
    fixedItemWidth: {
      type: Number,
      required: false,
    },
    selectable: {
      type: Boolean,
      default: true,
    },
    selected: {
      type: Array,
      default: () => [],
    },
    editFunction: {
      type: Function,
    },
    resizeFunction: {
      type: Function,
    },
    draggable: {
      type: Boolean,
      default: false,
    },
    deletable: {
      type: Boolean,
      default: false,
    },
    nowrap: {
      type: Boolean,
      default: false,
    },
    actionMenu: {
      type: Boolean,
      default: true,
    },
  },
  data: () => {
    return {
      selectedItems: [] as any[],
      localItems: [] as any[],
      localItemsIndex: {} as Record<string, any>,
      lastClickedNoShift: undefined as any,
      lastClickedShift: undefined as any,
      width: 150,
      transitions: true,
    }
  },
  watch: {
    selectedItems: {
      handler() {
        this.$emit('update:selected', this.selectedItems)
      },
      immediate: true,
    },
    selected: {
      handler() {
        this.selectedItems = this.selected as []
      },
      immediate: true,
    },
    items: {
      handler() {
        this.localItems = this.items as []
        this.localItemsIndex = {}
        for (const [i, value] of this.localItems.entries()) {
          this.$set(this.localItemsIndex, JSON.stringify(value), i)
        }
      },
      immediate: true,
    },
    localItems: {
      handler() {
        this.$emit('update:items', this.localItems)
      },
      immediate: true,
    },
  },
  computed: {
    flexClass(): string {
      return this.nowrap ? 'd-flex flex-nowrap' : 'd-flex flex-wrap'
    },
    hasItems(): boolean {
      return this.items.length > 0
    },
    itemWidth(): number {
      return this.fixedItemWidth ? this.fixedItemWidth : this.width
    },
    shouldPreselect(): boolean {
      return this.selectedItems.length > 0
    },
    dragOptions(): any {
      return {
        animation: 200,
        group: 'item-cards',
        disabled: !this.draggable,
        ghostClass: 'ghost',
      }
    },
  },
  methods: {
    // handle normal click and shift click
    handleSelectClick(toggle: () => void, item: any, e: MouseEvent) {
      if (!e.shiftKey) {
        this.lastClickedShift = undefined
        this.lastClickedNoShift = item
      } else {
        // if it's a shift click and we haven't clicked anywhere before, consider the first item as the beginning of the selection
        if (!this.lastClickedNoShift) this.lastClickedNoShift = this.$_.head(this.localItems)
      }

      if (e.shiftKey && this.lastClickedNoShift) {
        // recompute last shift selection so we can unselect it
        if (this.lastClickedShift) {
          const pf = (i: any) => i !== this.lastClickedShift && i !== this.lastClickedNoShift
          let previousShiftSelection = this.$_.dropRightWhile(this.$_.dropWhile(this.localItems, pf), pf)
          this.$_.pullAll(this.selectedItems, previousShiftSelection)
        }

        // compute shift selection and select it
        const f = (i: any) => i !== item && i !== this.lastClickedNoShift
        let shiftSelection = this.$_.dropRightWhile(this.$_.dropWhile(this.localItems, f), f)
        shiftSelection.forEach(i => {
          if (!this.selectedItems.includes(i)) this.selectedItems.push(i)
        })

        this.lastClickedShift = item
        return
      }

      // perform a normal toggle
      toggle()
    },
    onResize() {
      const content = this.$refs.content as HTMLElement
      this.width = computeCardWidth(content.clientWidth, this.$vuetify.breakpoint.name.toString())
    },
    deleteItem(item: any) {
      const index = this.localItems.findIndex((e: any) => e.id === item.id)
      this.localItems.splice(index, 1)
    },
    updateIndex(item: any) {
      const oldIndex = this.localItems.indexOf(item)
      const newIndex = Math.min(Math.max(this.localItemsIndex[JSON.stringify(item)], 0), this.localItems.length - 1)
      if (oldIndex != newIndex)
        this.localItems.splice(oldIndex, 1, this.localItems.splice(newIndex, 1, this.localItems[oldIndex])[0])
    },
  },
})
</script>

<style scoped>
.ghost .item-card {
  opacity: 0.5;
  background: #c8ebfb;
}

.handle {
  cursor: grab;
}

.flip-list-move {
  transition: transform 0.4s;
}
</style>
