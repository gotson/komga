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
              v-if="hover || selected || preselect"
              absolute
              :opacity="hover ? 0.3 : 0"
              :class="`${hover ? 'item-border-darken' : selected ? 'item-border' : 'item-border-transparent'} overlay-full`"
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
                v-if="bookReady && !selected && !preselect"
                fab
                x-large
                color="accent"
                style="position: absolute; top: 50%; left: 50%; margin-left: -36px; margin-top: -36px"
                :to="{name: 'read-book', params: { bookId: item.id}}"
              >
                <v-icon>mdi-book-open-page-variant</v-icon>
              </v-btn>

              <!-- Pen icon for edition (bottom left) -->
              <v-icon v-if="!selected && !preselect && onEdit"
                      style="position: absolute; bottom: 10px; left: 10px"
                      @click.stop="editItem"
              >
                mdi-pencil
              </v-icon>
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
          <v-card-subtitle
            v-line-clamp="2"
            v-bind="subtitleProps"
            v-html="title"
          >
          </v-card-subtitle>
          <v-card-text class="px-2" v-html="body">
          </v-card-text>
        </template>
      </v-card>
    </template>
  </v-hover>
</template>

<script lang="ts">
import { getReadProgress, getReadProgressPercentage } from '@/functions/book-progress'
import { ReadStatus } from '@/types/enum-books'
import { createItem, Item } from '@/types/items'
import Vue from 'vue'

export default Vue.extend({
  name: 'ItemCard',
  props: {
    item: {
      type: Object as () => BookDto | SeriesDto,
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
  },
  data: () => {
    return {}
  },
  computed: {
    overlay (): boolean {
      return this.onEdit !== undefined || this.onSelected !== undefined || this.bookReady
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
      if ('seriesId' in this.item) return getReadProgress(this.item) === ReadStatus.IN_PROGRESS
      return false
    },
    isUnread (): boolean {
      if ('seriesId' in this.item) return getReadProgress(this.item) === ReadStatus.UNREAD
      return false
    },
    unreadCount (): number | undefined {
      if (!('seriesId' in this.item)) return this.item.booksUnreadCount
      return undefined
    },
    readProgressPercentage (): number {
      if ('seriesId' in this.item) return getReadProgressPercentage(this.item)
      return 0
    },
    bookReady (): boolean {
      if ('seriesId' in this.item) {
        return this.item.media.status === 'READY'
      }
      return false
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

</style>
