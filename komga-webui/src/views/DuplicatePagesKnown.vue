<template>
  <v-container fluid class="pa-6">
    <v-alert type="warning" dismissible text class="body-2">
      <div>{{ $t('duplicate_pages.info') }}</div>
    </v-alert>

    <v-row align="center">
      <v-col cols="auto">
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />
      </v-col>

      <v-spacer/>

      <v-col>
        <v-select
          :items="selectOptions"
          v-model="filterActive"
          small-chips
          deletable-chips
          multiple
        >
        </v-select>
      </v-col>

      <v-col
        v-for="sortOption in sortOptions"
        :key="sortOption.key"
        cols="auto"
      >
        <v-btn
          rounded
          small
          :color="sortActive.key === sortOption.key ? 'primary' : ''"
          @click="setSort(sortOption.key)"
        >
          {{ sortOption.name }}
          <v-icon
            v-if="sortActive.key === sortOption.key"
            class="ms-2"
          >
            {{ sortActive.order === 'desc' ? 'mdi-sort-variant' : 'mdi-sort-reverse-variant' }}
          </v-icon>
        </v-btn>
      </v-col>

    </v-row>

    <empty-state
      v-if="totalPages === 0"
      :title="$t('duplicate_pages.empty_title_known')"
      icon="mdi-check"
      icon-color="success"
    />

    <v-row v-else>
      <v-slide-x-transition
        v-for="(element, i) in elements"
        :key="i"
      >
        <page-hash-known-card
          class="ma-2"
          :hash="element"
          @image-clicked="showDialogImage(element)"
          @matches-clicked="showDialogMatches(element)"
          @updated="pageHashUpdated"
        />
      </v-slide-x-transition>

    </v-row>

    <v-row>
      <v-col cols="12">
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />
      </v-col>
    </v-row>

    <v-dialog v-model="dialogImage">
      <v-card>
        <v-card-text>
          <v-img
            @click="dialogImage = false"
            contain
            :src="pageHashKnownThumbnailUrl(dialogImagePageHash)"
            style="cursor: zoom-out;"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog
      v-model="dialogMatches"
      scrollable
    >
      <v-card>
        <v-card-text>
          <page-hash-matches-table
            :hash="dialogMatchesPageHash"
            class="my-2"
          />
        </v-card-text>
        <v-card-actions>
          <v-btn @click="dialogMatches = false" text>{{ $t('common.close') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {PageHashDto, PageHashKnownDto} from '@/types/komga-pagehashes'
import {pageHashKnownThumbnailUrl} from '@/functions/urls'
import PageHashKnownCard from '@/components/PageHashKnownCard.vue'
import {PageHashAction} from '@/types/enum-pagehashes'
import PageHashMatchesTable from '@/components/PageHashMatchesTable.vue'
import EmptyState from '@/components/EmptyState.vue'

export default Vue.extend({
  name: 'DuplicatePagesKnown',
  components: {EmptyState, PageHashKnownCard, PageHashMatchesTable},
  data: function () {
    return {
      pageHashKnownThumbnailUrl,
      elements: [] as PageHashKnownDto[],
      totalElements: 0,
      page: 1,
      totalPages: 0,
      sortActive: {key: 'deleteSize', order: 'desc'} as SortActive,
      filterActive: [PageHashAction.DELETE_AUTO, PageHashAction.DELETE_MANUAL],
      dialogImage: false,
      dialogMatches: false,
      dialogImagePageHash: {} as PageHashKnownDto,
      dialogMatchesPageHash: {} as PageHashDto,
    }
  },
  async mounted() {
    await this.loadData(this.page, this.sortActive, this.filterActive)
  },
  watch: {
    page(val) {
      this.loadData(val, this.sortActive, this.filterActive)
    },
    sortActive(val) {
      this.loadData(this.page, val, this.filterActive)
    },
    filterActive(val) {
      this.loadData(this.page, this.sortActive, val)
    },
  },
  computed: {
    selectOptions(): object[] {
      return Object.keys(PageHashAction).map(x => ({
        text: this.$t(`enums.page_hash_action.${x.valueOf()}`),
        value: x.valueOf(),
      }))
    },
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('duplicate_pages.filter.delete_size').toString(), key: 'deleteSize'},
        {name: this.$t('duplicate_pages.filter.size').toString(), key: 'fileSize'},
        {name: this.$t('duplicate_pages.filter.delete_count').toString(), key: 'deleteCount'},
        {name: this.$t('duplicate_pages.filter.match_count').toString(), key: 'matchCount'},
        {name: this.$t('duplicate_pages.filter.date_added').toString(), key: 'createdDate'},
        {name: this.$t('duplicate_pages.filter.date_modified').toString(), key: 'lastModifiedDate'},
      ]
    },
    paginationVisible(): number {
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
          return 5
        case 'sm':
        case 'md':
          return 10
        case 'lg':
        case 'xl':
        default:
          return 15
      }
    },
  },
  methods: {
    async loadData(page: number, sort: SortActive, actions: string[]) {
      const pageRequest = {
        page: page - 1,
        sort: [`${sort.key},${sort.order}`],
      } as PageRequest

      const elementsPage = await this.$komgaPageHashes.getKnownHashes(actions, pageRequest)
      this.totalElements = elementsPage.totalElements
      this.totalPages = elementsPage.totalPages
      this.elements = elementsPage.content
    },
    setSort(key: string) {
      if (this.sortActive.key === key) {
        if (this.sortActive.order === 'desc') {
          this.sortActive = {key: key, order: 'asc'}
        } else {
          this.sortActive = {key: key, order: 'desc'}
        }
      } else {
        this.sortActive = {key: key, order: 'desc'}
      }
    },
    showDialogImage(pageHash: PageHashKnownDto) {
      this.dialogImagePageHash = pageHash
      this.dialogImage = true
    },
    showDialogMatches(pageHash: PageHashDto) {
      this.dialogMatchesPageHash = pageHash
      this.dialogMatches = true
    },
    pageHashUpdated(updated: PageHashKnownDto) {
      this.elements.find(x => {
          if (x.hash === updated.hash) {
            x.action = updated.action
            return true
          }
          return false
        },
      )
    },
  },
})
</script>

<style scoped>

</style>
