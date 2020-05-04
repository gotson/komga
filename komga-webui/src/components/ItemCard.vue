<template>
<v-hover :disabled="disableHover">
  <template v-slot:default="{ hover }">
    <v-card
      :width="width"
      @click="onClick"
      :ripple="false"
    >
      <!--      Thumbnail-->
      <v-img
        :src="thumbnailUrl"
        lazy-src="../assets/cover.svg"
        aspect-ratio="0.7071"
      >
        <v-fade-transition>
          <v-overlay
            v-if="hover || selected || preselect"
            absolute
            :opacity="hover ? 0.3 : 0"
            :class="`${hover ? 'item-border-darken' : selected ? 'item-border' : 'item-border-transparent'} overlay-full`"
          >
            <v-icon v-if="onSelected"
                    :color="selected ? 'secondary' : ''"
                    style="position: absolute; top: 5px; left: 10px"
                    @click.stop="selectItem"
            >
              {{ selected || (preselect && hover) ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline'
              }}
            </v-icon>

            <v-icon v-if="!selected && !preselect && onEdit"
                    style="position: absolute; bottom: 10px; left: 10px"
                    @click.stop="editItem"
            >
              mdi-pencil
            </v-icon>
          </v-overlay>
        </v-fade-transition>
      </v-img>
      <!--      Description-->
      <v-card-subtitle
        v-line-clamp="2"
        v-bind="subtitleProps"
        v-html="subtitle"
      >
      </v-card-subtitle>
      <v-card-text class="px-2" v-html="body">
      </v-card-text>
    </v-card>
  </template>
</v-hover>
</template>

<script lang="ts">
import Vue from 'vue'
import { BookItem, createItem, Item } from '@/types/items'

export default Vue.extend({
  name: 'ItemCard',
  props: {
    item: {
      type: Object as () => BookDto | SeriesDto,
      required: true,
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150,
    },
    selected: {
      type: Boolean,
      default: false,
    },
    preselect: {
      type: Boolean,
      required: false,
    },
    onSelected: {
      type: Function,
      default: undefined,
      required: false,
    },
    onEdit: {
      type: Function,
      default: undefined,
      required: false,
    },
  },
  data: () => {
    return {
    }
  },
  computed: {
    overlay (): boolean {
      return this.onEdit !== undefined || this.onSelected !== undefined
    },
    computedItem (): Item<BookDto | SeriesDto> {
      return createItem(this.item)
    },
    disableHover (): boolean {
      return !this.overlay
    },
    thumbnailUrl (): string {
      return this.computedItem.thumbnailUrl()
    },
    subtitle (): string {
      return this.computedItem.subtitle()
    },
    subtitleProps (): Object {
      return this.computedItem.subtitleProps()
    },
    body (): string {
      return this.computedItem.body()
    },
  },
  methods: {
    onClick () {
      if (this.preselect && this.onSelected !== undefined) {
        this.selectItem()
      } else {
        this.goto()
      }
    },
    goto () {
      this.computedItem.goto(this.$router)
    },
    selectItem () {
      if (this.onSelected !== undefined) {
        this.onSelected()
      }
    },
    editItem () {
      if (this.onEdit !== undefined) {
        this.onEdit(this.item)
      }
    },
  },
})
</script>

<style>
  .item-border {
    border: 3px solid var(--v-secondary-base);
  }

  .item-border-transparent {
    border: 3px solid transparent;
  }

  .item-border-darken {
    border: 3px solid var(--v-secondary-darken2);
  }

  .overlay-full .v-overlay__content {
    width: 100%;
    height: 100%;
  }
</style>
