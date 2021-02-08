<template>
  <v-bottom-navigation
    v-if="collectionsCount > 0 || readListsCount > 0"
    grow color="primary"
    :fixed="$vuetify.breakpoint.name === 'xs'"
  >
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
  },
  watch: {
    libraryId: {
      handler (val) {
        this.loadCounts(val)
      },
      immediate: true,
    },
  },
  created () {
    this.$eventHub.$on(COLLECTION_CHANGED, this.reloadCounts)
    this.$eventHub.$on(READLIST_CHANGED, this.reloadCounts)
  },
  beforeDestroy () {
    this.$eventHub.$off(COLLECTION_CHANGED, this.reloadCounts)
    this.$eventHub.$off(READLIST_CHANGED, this.reloadCounts)
  },
  methods: {
    reloadCounts () {
      this.loadCounts(this.libraryId)
    },
    async loadCounts (libraryId: string) {
      const lib = libraryId !== LIBRARIES_ALL ? [libraryId] : undefined
      this.collectionsCount = (await this.$komgaCollections.getCollections(lib, { size: 1 })).totalElements
      this.readListsCount = (await this.$komgaReadLists.getReadLists(lib, { size: 1 })).totalElements
    },
  },
})
</script>

<style scoped>

</style>
