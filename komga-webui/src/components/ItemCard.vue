<template>
  <v-hover :disabled="disableHover">
    <template v-slot:default="{ hover }">
      <v-card
        :width="width"
        @click="onClick"
        :class="noLink ? 'no-link' : ''"
        :ripple="false"
      >
        <!--      Thumbnail-->
        <v-img
          :src="thumbnailUrl"
          lazy-src="../assets/cover.svg"
          aspect-ratio="0.7071"
          contain
        >
          <!-- unread tick for book -->
          <div class="unread" v-if="isUnread"/>

          <!-- unread count for series -->
          <span v-if="unreadCount"
                class="white--text pa-1 px-2 subtitle-2"
                :style="{background: 'orange', position: 'absolute', right: 0}"
          >
            {{ unreadCount }}
          </span>

          <!-- Thumbnail overlay -->
          <v-fade-transition>
            <v-overlay
              v-if="hover || selected || preselect || actionMenuState"
              absolute
              :opacity="hover || actionMenuState ? 0.3 : 0"
              :class="`${hover || actionMenuState ? 'item-border-darken' : selected ? 'item-border' : 'item-border-transparent'} overlay-full`"
            >
              <!-- Circle icon for selection (top left) -->
              <v-icon v-if="onSelected"
                      :color="selected ? 'secondary' : ''"
                      style="position: absolute; top: 5px; left: 10px"
                      @click.stop="selectItem"
              >
                {{ selected || (preselect && hover) ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline'
                }}
              </v-icon>

              <!-- FAB reading (center) -->
              <v-btn
                v-if="bookReady && !selected && !preselect && canReadPages"
                fab
                x-large
                color="accent"
                style="position: absolute; top: 50%; left: 50%; margin-left: -36px; margin-top: -36px"
                :to="{name: 'read-book', params: { bookId: item.id}}"
              >
                <v-icon>mdi-book-open-page-variant</v-icon>
              </v-btn>

              <!-- Pen icon for edition (bottom left) -->
              <v-btn icon
                     v-if="!selected && !preselect && onEdit"
                     style="position: absolute; bottom: 5px; left: 5px"
                     @click.stop="editItem"
              >
                <v-icon>mdi-pencil</v-icon>
              </v-btn>

              <!-- Action menu (bottom right) -->
              <div v-if="!selected && !preselect && actionMenu"
                   style="position: absolute; bottom: 5px; right: 5px"
              >
                <book-actions-menu v-if="computedItem.type() === ItemTypes.BOOK"
                                   :book="item"
                                   :menu.sync="actionMenuState"
                />
                <series-actions-menu v-if="computedItem.type() === ItemTypes.SERIES"
                                     :series="item"
                                     :menu.sync="actionMenuState"
                />
                <collection-actions-menu v-if="computedItem.type() === ItemTypes.COLLECTION"
                                         :collection="item"
                                         :menu.sync="actionMenuState"
                />
              </div>
            </v-overlay>
          </v-fade-transition>
          <v-progress-linear
            v-if="isInProgress"
            :value="readProgressPercentage"
            color="orange"
            height="6"
            style="position: absolute; bottom: 0"
          />
        </v-img>

        <!--      Description-->
        <template v-if="!thumbnailOnly">
          <router-link :to="to" class="link-underline">
            <v-card-subtitle
              v-line-clamp="2"
              v-bind="subtitleProps"
              v-html="title"
            >
            </v-card-subtitle>
          </router-link>
          <v-card-text class="px-2" v-html="body">
          </v-card-text>
        </template>
      </v-card>
    </template>
  </v-hover>
</template>

<script lang="ts">
import BookActionsMenu from '@/components/menus/BookActionsMenu.vue'
import CollectionActionsMenu from '@/components/menus/CollectionActionsMenu.vue'
import SeriesActionsMenu from '@/components/menus/SeriesActionsMenu.vue'
import { getReadProgress, getReadProgressPercentage } from '@/functions/book-progress'
import { ReadStatus } from '@/types/enum-books'
import { createItem, Item, ItemTypes } from '@/types/items'
import Vue from 'vue'
import { RawLocation } from 'vue-router'

export default Vue.extend({
  name: 'ItemCard',
  components: { BookActionsMenu, SeriesActionsMenu, CollectionActionsMenu },
  props: {
    item: {
      type: Object as () => BookDto | SeriesDto | CollectionDto,
      required: true,
    },
    // hide the bottom part of the card
    thumbnailOnly: {
      type: Boolean,
      default: false,
    },
    // disables the default link on clicking the card
    noLink: {
      type: Boolean,
      default: false,
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150,
    },
    // when true, card will show the active border and circle icon full
    selected: {
      type: Boolean,
      default: false,
    },
    // when true, will display the border like if the card was hovered, and click anywhere will trigger onSelected
    preselect: {
      type: Boolean,
      required: false,
    },
    // callback function to call when selecting the card
    onSelected: {
      type: Function,
      default: undefined,
      required: false,
    },
    // callback function for the edit button
    onEdit: {
      type: Function,
      default: undefined,
      required: false,
    },
    // action menu enabled or not
    actionMenu: {
      type: Boolean,
      default: true,
    },
  },
  data: () => {
    return {
      ItemTypes,
      actionMenuState: false,
    }
  },
  computed: {
    canReadPages (): boolean {
      return this.$store.getters.mePageStreaming && this.computedItem.type() === ItemTypes.BOOK
    },
    overlay (): boolean {
      return this.onEdit !== undefined || this.onSelected !== undefined || this.bookReady || this.canReadPages || this.actionMenu
    },
    computedItem (): Item<BookDto | SeriesDto | CollectionDto> {
      return createItem(this.item)
    },
    disableHover (): boolean {
      return !this.overlay
    },
    thumbnailUrl (): string {
      return this.computedItem.thumbnailUrl()
    },
    title (): string {
      return this.computedItem.title()
    },
    subtitleProps (): Object {
      return this.computedItem.subtitleProps()
    },
    body (): string {
      return this.computedItem.body()
    },
    isInProgress (): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgress(this.item as BookDto) === ReadStatus.IN_PROGRESS
      return false
    },
    isUnread (): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgress(this.item as BookDto) === ReadStatus.UNREAD
      return false
    },
    unreadCount (): number | undefined {
      if (this.computedItem.type() === ItemTypes.SERIES) return (this.item as SeriesDto).booksUnreadCount
      return undefined
    },
    readProgressPercentage (): number {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgressPercentage(this.item as BookDto)
      return 0
    },
    bookReady (): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) {
        return (this.item as BookDto).media.status === 'READY'
      }
      return false
    },
    to (): RawLocation {
      return this.computedItem.to()
    },
  },
  methods: {
    onClick () {
      if (this.preselect && this.onSelected !== undefined) {
        this.selectItem()
      } else if (!this.noLink) {
        this.goto()
      }
    },
    goto () {
      this.$router.push(this.computedItem.to())
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
.no-link {
  cursor: default;
}

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

.unread {
  border-left: 25px solid transparent;
  border-right: 25px solid orange;
  border-bottom: 25px solid transparent;
  height: 0;
  width: 0;
  position: absolute;
  right: 0;
  z-index: 2;
}

.link-underline {
  text-decoration: none;
}

.link-underline:hover {
  text-decoration: underline;
  text-decoration-color: black;
}
</style>
