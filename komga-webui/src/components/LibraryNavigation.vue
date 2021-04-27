<template>
  <div>
    <v-bottom-navigation
      v-if="show && bottomNavigation"
      grow color="primary"
      :fixed="$vuetify.breakpoint.name === 'xs'"
    >
      <v-btn v-if="showRecommended"
             :to="{name: 'recommended-libraries', params: {libraryId: libraryId}}"
      >
        <span>{{ $t('library_navigation.recommended') }}</span>
        <v-icon>mdi-star</v-icon>
      </v-btn>

      <v-btn :to="{name: 'browse-libraries', params: {libraryId: libraryId}}">
        <span>{{ $t('library_navigation.browse') }}</span>
        <v-icon>mdi-bookshelf</v-icon>
      </v-btn>

      <v-btn
        v-if="collectionsCount > 0"
        :to="{name: 'browse-collections', params: {libraryId: libraryId}}"
      >
        <span>{{ $t('library_navigation.collections') }}</span>
        <v-icon>mdi-layers-triple</v-icon>
      </v-btn>

      <v-btn
        v-if="readListsCount > 0"
        :to="{name: 'browse-readlists', params: {libraryId: libraryId}}"
      >
        <span>{{ $t('library_navigation.readlists') }}</span>
        <v-icon>mdi-book-multiple</v-icon>
      </v-btn>

    </v-bottom-navigation>

    <template
      v-if="show && !bottomNavigation"
    >
      <v-btn v-if="showRecommended"
             :to="{name: 'recommended-libraries', params: {libraryId: libraryId}}"
             text
             class="mx-1"
      >
        {{ $t('library_navigation.recommended') }}
      </v-btn>

      <v-btn :to="{name: 'browse-libraries', params: {libraryId: libraryId}}"
             text
             class="mx-1"
      >
        {{ $t('library_navigation.browse') }}
      </v-btn>

      <v-btn
        v-if="collectionsCount > 0"
        :to="{name: 'browse-collections', params: {libraryId: libraryId}}"
        text
        class="mx-1"
      >
        {{ $t('library_navigation.collections') }}
      </v-btn>

      <v-btn
        v-if="readListsCount > 0"
        :to="{name: 'browse-readlists', params: {libraryId: libraryId}}"
        text
        class="mx-1"
      >
        {{ $t('library_navigation.readlists') }}
      </v-btn>

    </template>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {COLLECTION_CHANGED, READLIST_CHANGED} from '@/types/events'
import {LIBRARIES_ALL} from '@/types/library'

export default Vue.extend({
  name: 'LibraryNavigation',
  data: () => {
    return {
      collectionsCount: 0,
      readListsCount: 0,
    }
  },
  props: {
    libraryId: {
      type: String,
      required: true,
    },
    bottomNavigation: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    libraryId: {
      handler(val) {
        this.loadCounts(val)
      },
      immediate: true,
    },
  },
  created() {
    this.$eventHub.$on(COLLECTION_CHANGED, this.reloadCounts)
    this.$eventHub.$on(READLIST_CHANGED, this.reloadCounts)
  },
  beforeDestroy() {
    this.$eventHub.$off(COLLECTION_CHANGED, this.reloadCounts)
    this.$eventHub.$off(READLIST_CHANGED, this.reloadCounts)
  },
  computed: {
    showRecommended(): boolean {
      return this.libraryId !== LIBRARIES_ALL
    },
    show(): boolean {
      return this.collectionsCount > 0 || this.readListsCount > 0 || this.showRecommended
    },
  },
  methods: {
    reloadCounts() {
      this.loadCounts(this.libraryId)
    },
    async loadCounts(libraryId: string) {
      const lib = libraryId !== LIBRARIES_ALL ? [libraryId] : undefined
      this.collectionsCount = (await this.$komgaCollections.getCollections(lib, {size: 1})).totalElements
      this.readListsCount = (await this.$komgaReadLists.getReadLists(lib, {size: 1})).totalElements
    },
  },
})
</script>

<style scoped>

</style>
