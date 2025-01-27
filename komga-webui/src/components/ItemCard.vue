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
          :lazy-src="thumbnailError ? coverBase64 : undefined"
          aspect-ratio="0.7071"
          contain
          @error="thumbnailError = true"
          @load="thumbnailError = false"
        >
          <!-- unread tick for book -->
          <div class="unread" v-if="isUnread"/>

          <!-- unread count for series -->
          <span v-else-if="unreadCount"
                class="white--text pa-1 px-2 text-subtitle-2"
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
                      :style="'position: absolute; top: 5px; ' + ($vuetify.rtl ? 'right' : 'left') + ': 10px'"
                      @click.stop="selectItem"
              >
                {{
                  selected || (preselect && hover) ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline'
                }}
              </v-icon>

              <!-- FAB reading (center) -->
              <v-btn
                v-if="showFab"
                fab
                x-large
                color="accent"
                style="position: absolute; top: 50%; left: 50%; margin-left: -36px; margin-top: -36px"
                :to="fabTo"
                @click.native="$event.stopImmediatePropagation()"
              >
                <v-icon>mdi-book-open-page-variant</v-icon>
              </v-btn>

              <!-- Pen icon for edition (bottom left) -->
              <v-btn icon
                     v-if="!selected && !preselect && onEdit"
                     :style="'position: absolute; bottom: 5px; ' + ($vuetify.rtl ? 'right' : 'left' ) +': 5px'"
                     @click.stop="editItem"
              >
                <v-icon>mdi-pencil</v-icon>
              </v-btn>

              <!-- Action menu (bottom right) -->
              <div v-if="!selected && !preselect && actionMenu"
                   :style="'position: absolute; bottom: 5px; ' + ($vuetify.rtl ? 'left' : 'right') +': 5px'"
              >
                <one-shot-actions-menu v-if="computedItem.type() === ItemTypes.BOOK && item.oneshot"
                                       :book="item"
                                       :menu.sync="actionMenuState"
                />
                <book-actions-menu v-if="computedItem.type() === ItemTypes.BOOK && !item.oneshot"
                                   :book="item"
                                   :menu.sync="actionMenuState"
                />
                <one-shot-actions-menu v-if="computedItem.type() === ItemTypes.SERIES && item.oneshot"
                                       :series="item"
                                       :menu.sync="actionMenuState"
                />
                <series-actions-menu v-if="computedItem.type() === ItemTypes.SERIES && !item.oneshot"
                                     :series="item"
                                     :menu.sync="actionMenuState"
                />
                <collection-actions-menu v-if="computedItem.type() === ItemTypes.COLLECTION"
                                         :collection="item"
                                         :menu.sync="actionMenuState"
                />
                <read-list-actions-menu v-if="computedItem.type() === ItemTypes.READLIST"
                                        :read-list="item"
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
          <router-link v-if="!Array.isArray(title)" :to="title.to" class="link-underline"
                       @click.native="$event.stopImmediatePropagation()">
            <v-card-subtitle
              v-line-clamp="2"
              v-bind="subtitleProps"
              :title="title.title"
            >{{ title.title }}
            </v-card-subtitle>
          </router-link>
          <template v-if="Array.isArray(title)">
            <v-card-subtitle
              v-bind="subtitleProps"
            >
              <router-link
                v-for="(t, i) in title"
                :key="i"
                :to="t.to"
                @click.native="$event.stopImmediatePropagation()"
                class="link-underline text-truncate"
                :title="t.title"
                style="display: block"
                :class="i !== 0 ? 'font-weight-light' : ''"
              >{{ t.title }}
              </router-link>
            </v-card-subtitle>
          </template>
          <v-card-text class="px-2 pt-0 font-weight-light" v-html="body">
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
import {getReadProgress, getReadProgressPercentage} from '@/functions/book-progress'
import {ReadStatus} from '@/types/enum-books'
import {createItem, Item, ItemContext, ItemTitle, ItemTypes} from '@/types/items'
import Vue from 'vue'
import {RawLocation} from 'vue-router'
import ReadListActionsMenu from '@/components/menus/ReadListActionsMenu.vue'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from '@/types/komga-series'
import {
  THUMBNAILBOOK_ADDED,
  THUMBNAILBOOK_DELETED,
  THUMBNAILCOLLECTION_ADDED,
  THUMBNAILCOLLECTION_DELETED,
  THUMBNAILREADLIST_ADDED,
  THUMBNAILREADLIST_DELETED,
  THUMBNAILSERIES_ADDED,
  THUMBNAILSERIES_DELETED,
} from '@/types/events'
import {
  ThumbnailBookSseDto,
  ThumbnailCollectionSseDto,
  ThumbnailReadListSseDto,
  ThumbnailSeriesSseDto,
} from '@/types/komga-sse'
import {coverBase64} from '@/types/image'
import {ReadListDto} from '@/types/komga-readlists'
import OneShotActionsMenu from '@/components/menus/OneshotActionsMenu.vue'

export default Vue.extend({
  name: 'ItemCard',
  components: {OneShotActionsMenu, BookActionsMenu, SeriesActionsMenu, CollectionActionsMenu, ReadListActionsMenu},
  props: {
    item: {
      type: Object as () => BookDto | SeriesDto | CollectionDto | ReadListDto,
      required: true,
    },
    itemContext: {
      type: Array as () => ItemContext[],
      default: () => [],
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
    // force disable fab
    disableFab: {
      type: Boolean,
      default: false,
    },
  },
  data: () => {
    return {
      ItemTypes,
      actionMenuState: false,
      thumbnailError: false,
      thumbnailCacheBust: '',
      coverBase64,
    }
  },
  created() {
    this.$eventHub.$on(THUMBNAILBOOK_ADDED, this.thumbnailBookChanged)
    this.$eventHub.$on(THUMBNAILBOOK_DELETED, this.thumbnailBookChanged)

    this.$eventHub.$on(THUMBNAILSERIES_ADDED, this.thumbnailSeriesChanged)
    this.$eventHub.$on(THUMBNAILSERIES_DELETED, this.thumbnailSeriesChanged)

    this.$eventHub.$on(THUMBNAILREADLIST_ADDED, this.thumbnailReadListChanged)
    this.$eventHub.$on(THUMBNAILREADLIST_DELETED, this.thumbnailReadListChanged)

    this.$eventHub.$on(THUMBNAILCOLLECTION_ADDED, this.thumbnailCollectionChanged)
    this.$eventHub.$on(THUMBNAILCOLLECTION_DELETED, this.thumbnailCollectionChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(THUMBNAILBOOK_ADDED, this.thumbnailBookChanged)
    this.$eventHub.$off(THUMBNAILBOOK_DELETED, this.thumbnailBookChanged)

    this.$eventHub.$off(THUMBNAILSERIES_ADDED, this.thumbnailSeriesChanged)
    this.$eventHub.$off(THUMBNAILSERIES_DELETED, this.thumbnailSeriesChanged)

    this.$eventHub.$off(THUMBNAILREADLIST_ADDED, this.thumbnailReadListChanged)
    this.$eventHub.$off(THUMBNAILREADLIST_DELETED, this.thumbnailReadListChanged)

    this.$eventHub.$off(THUMBNAILCOLLECTION_ADDED, this.thumbnailCollectionChanged)
    this.$eventHub.$off(THUMBNAILCOLLECTION_DELETED, this.thumbnailCollectionChanged)
  },
  computed: {
    canReadPages(): boolean {
      return this.$store.getters.mePageStreaming && this.computedItem.type() === ItemTypes.BOOK
    },
    overlay(): boolean {
      return this.onEdit !== undefined || this.onSelected !== undefined || this.showFab || this.actionMenu
    },
    computedItem(): Item<BookDto | SeriesDto | CollectionDto | ReadListDto> {
      let item = this.item
      if ('libraryId' in this.item && this.$store.getters.getLibraryById((this.item as any).libraryId).unavailable)
        item = {...item, deleted: true}
      return createItem(item)
    },
    disableHover(): boolean {
      return !this.overlay
    },
    thumbnailUrl(): string {
      return this.computedItem.thumbnailUrl() + this.thumbnailCacheBust
    },
    title(): ItemTitle | ItemTitle[] {
      return this.computedItem.title(this.itemContext)
    },
    subtitleProps(): Object {
      return this.computedItem.subtitleProps()
    },
    body(): string {
      return this.computedItem.body(this.itemContext)
    },
    isInProgress(): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgress(this.item as BookDto) === ReadStatus.IN_PROGRESS
      return false
    },
    isUnread(): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgress(this.item as BookDto) === ReadStatus.UNREAD
      if (this.computedItem.type() === ItemTypes.SERIES && (this.item as SeriesDto).oneshot) return (this.item as SeriesDto).booksUnreadCount + (this.item as SeriesDto).booksInProgressCount > 0
      return false
    },
    unreadCount(): number | undefined {
      if (this.computedItem.type() === ItemTypes.SERIES) return (this.item as SeriesDto).booksUnreadCount + (this.item as SeriesDto).booksInProgressCount
      return undefined
    },
    readProgressPercentage(): number {
      if (this.computedItem.type() === ItemTypes.BOOK) return getReadProgressPercentage(this.item as BookDto)
      return 0
    },
    bookReady(): boolean {
      if (this.computedItem.type() === ItemTypes.BOOK) {
        return (this.item as BookDto).media.status === 'READY'
      }
      return false
    },
    showFab(): boolean {
      return !this.disableFab && this.bookReady && !this.selected && !this.preselect && this.canReadPages
    },
    to(): RawLocation {
      return this.computedItem.to()
    },
    fabTo(): RawLocation {
      return this.computedItem.fabTo()
    },
  },
  methods: {
    thumbnailBookChanged(event: ThumbnailBookSseDto) {
      if (event.selected && (this.computedItem.type() === ItemTypes.BOOK && event.bookId === this.item.id)
        || (this.thumbnailError && this.computedItem.type() === ItemTypes.SERIES && event.seriesId === this.item.id)
      ) {
        this.thumbnailCacheBust = '?' + this.$_.random(1000)
      }
    },
    thumbnailSeriesChanged(event: ThumbnailSeriesSseDto) {
      if (event.selected && this.computedItem.type() === ItemTypes.SERIES && event.seriesId === this.item.id) {
        this.thumbnailCacheBust = '?' + this.$_.random(1000)
      }
    },
    thumbnailReadListChanged(event: ThumbnailReadListSseDto) {
      if (event.selected && this.computedItem.type() === ItemTypes.READLIST && event.readListId === this.item.id) {
        this.thumbnailCacheBust = '?' + this.$_.random(1000)
      }
    },
    thumbnailCollectionChanged(event: ThumbnailCollectionSseDto) {
      if (event.selected && this.computedItem.type() === ItemTypes.COLLECTION && event.collectionId === this.item.id) {
        this.thumbnailCacheBust = '?' + this.$_.random(1000)
      }
    },
    onClick(e: MouseEvent) {
      if (this.preselect && this.onSelected !== undefined) {
        this.selectItem(e)
      } else if (!this.noLink) {
        this.goto()
      }
    },
    goto() {
      this.$router.push(this.computedItem.to())
    },
    selectItem(e: MouseEvent) {
      if (this.onSelected !== undefined) {
        this.onSelected(this.item, e)
      }
    },
    editItem() {
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
