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
      >
        <transition-group type="transition"
                          :name="!draggable ? 'flip-list' : null"
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
                        :width="itemWidth"
                        :selected="active"
                        :no-link="draggable || deletable"
                        :preselect="shouldPreselect"
                        :onEdit="(draggable || deletable) ? undefined : editFunction"
                        :onSelected="(draggable || deletable) ? undefined : selectable ? toggle: undefined"
                        :action-menu="actionMenu"
                ></item-card>

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
import { computeCardWidth } from '@/functions/grid-utilities'
import Vue from 'vue'
import draggable from 'vuedraggable'

export default Vue.extend({
  name: 'ItemBrowser',
  components: { ItemCard, draggable },
  props: {
    items: {
      type: Array,
      required: true,
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
      selectedItems: [],
      localItems: [],
      width: 150,
    }
  },
  watch: {
    selectedItems: {
      handler () {
        this.$emit('update:selected', this.selectedItems)
      },
      immediate: true,
    },
    selected: {
      handler () {
        this.selectedItems = this.selected as []
      },
      immediate: true,
    },
    items: {
      handler () {
        this.localItems = this.items as []
      },
      immediate: true,
    },
    localItems: {
      handler () {
        this.$emit('update:items', this.localItems)
      },
      immediate: true,
    },
  },
  computed: {
    flexClass (): string {
      return this.nowrap ? 'd-flex flex-nowrap' : 'd-flex flex-wrap'
    },
    hasItems (): boolean {
      return this.items.length > 0
    },
    itemWidth (): number {
      return this.fixedItemWidth ? this.fixedItemWidth : this.width
    },
    shouldPreselect (): boolean {
      return this.selectedItems.length > 0
    },
    dragOptions (): any {
      return {
        animation: 200,
        group: 'item-cards',
        disabled: !this.draggable,
        ghostClass: 'ghost',
      }
    },
  },
  methods: {
    onResize () {
      const content = this.$refs.content as HTMLElement
      this.width = computeCardWidth(content.clientWidth, this.$vuetify.breakpoint.name)
    },
    deleteItem (item: any) {
      const index = this.localItems.findIndex((e: any) => e.id === item.id)
      this.localItems.splice(index, 1)
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
