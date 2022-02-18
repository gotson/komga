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

      <v-col>
        <page-size-select v-model="pageSize" :items="[1, 2, 5, 10, 20]"/>
      </v-col>

    </v-row>

    <v-row>
      <v-slide-x-transition
        v-for="(element, i) in elements"
        :key="i"
      >
        <page-hash-unknown-card
          v-show="!hiddenElements.includes(element)"
          class="ma-2"
          :hash="element"
          @image-clicked="showDialogImage(element)"
          @matches-clicked="showDialogMatches(element)"
          @created="pageHashCreated(element)"
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
            :src="pageHashUnknownThumbnailUrl(dialogImagePageHash)"
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
import {PageHashDto, PageHashUnknownDto} from '@/types/komga-pagehashes'
import {pageHashUnknownThumbnailUrl} from '@/functions/urls'
import PageHashMatchesTable from '@/components/PageHashMatchesTable.vue'
import PageHashUnknownCard from '@/components/PageHashUnknownCard.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'

export default Vue.extend({
  name: 'DuplicatePagesUnknown',
  components: {PageHashUnknownCard, PageHashMatchesTable, PageSizeSelect},
  data: function () {
    return {
      elements: [] as PageHashUnknownDto[],
      hiddenElements: [] as PageHashUnknownDto[],
      totalElements: 0,
      page: 1,
      totalPages: 1,
      sortActive: {key: 'totalSize', order: 'desc'} as SortActive,
      dialogImage: false,
      dialogMatches: false,
      dialogImagePageHash: {} as PageHashUnknownDto,
      dialogMatchesPageHash: {} as PageHashDto,
      pageHashUnknownThumbnailUrl,
    }
  },
  async mounted() {
    await this.loadData(this.page, this.sortActive)
  },
  watch: {
    page(val) {
      this.loadData(val, this.sortActive)
    },
    sortActive(val) {
      this.loadData(this.page, val)
    },
  },
  computed: {
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('duplicate_pages.filter.total_size').toString(), key: 'totalSize'},
        {name: this.$t('duplicate_pages.filter.size').toString(), key: 'fileSize'},
        {name: this.$t('duplicate_pages.filter.count').toString(), key: 'matchCount'},
      ]
    },
    paginationVisible(): number {
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
        case 'sm':
        case 'md':
          return 5
        case 'lg':
          return 10
        case 'xl':
        default:
          return 15
      }
    },
    pageSize: {
      get: function (): number {
        return this.$store.state.persistedState.duplicatesNewPageSize
      },
      set: function (value: number): void {
        this.$store.commit('setDuplicatesNewPageSize', value)
        this.loadData(this.page, this.sortActive)
      },
    },
  },
  methods: {
    async loadData(page: number, sort: SortActive) {
      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
        sort: [`${sort.key},${sort.order}`],
      } as PageRequest

      const itemsPage = await this.$komgaPageHashes.getUnknownHashes(pageRequest)
      this.totalElements = itemsPage.totalElements
      this.totalPages = itemsPage.totalPages
      this.elements = itemsPage.content
      if (this.page > this.totalPages) this.page = this.totalPages
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
    showDialogImage(pageHash: PageHashUnknownDto) {
      this.dialogImagePageHash = pageHash
      this.dialogImage = true
    },
    showDialogMatches(pageHash: PageHashDto) {
      this.dialogMatchesPageHash = pageHash
      this.dialogMatches = true
    },
    pageHashCreated(pageHash: PageHashUnknownDto) {
      this.hiddenElements.push(pageHash)
      if (this.elements.every(x => this.hiddenElements.includes(x))) {
        this.loadData(this.page, this.sortActive)
      }
    },
  },
})
</script>

<style scoped>

</style>
