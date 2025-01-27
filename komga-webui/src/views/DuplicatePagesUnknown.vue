<template>
  <v-container fluid class="pa-6">
    <v-alert type="warning" dismissible text class="body-2">
      <div>{{ $t('duplicate_pages.info') }}</div>
    </v-alert>

    <empty-state
      v-if="totalPages === 0"
      :title="$t('duplicate_pages.empty_title')"
      icon="mdi-check"
      icon-color="success"
    />

    <template v-else>
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
          <page-size-select v-model="pageSize" :items="[1, 2, 5, 10, 20, 50]"/>
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

      <v-row justify="center">
        <v-col cols="auto">
          <v-btn @click="actionRemaining(PageHashAction.IGNORE)"
                 :disabled="remainingCount < 1"
          >{{ $t('duplicate_pages.action_ignore_remaining', {count: remainingCount}) }}
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn @click="confirmRemaining(PageHashAction.DELETE_MANUAL)"
                 :disabled="remainingCount < 1"
                 color="warning"
          >{{ $t('duplicate_pages.action_manual_delete_remaining', {count: remainingCount}) }}
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn @click="confirmRemaining(PageHashAction.DELETE_AUTO)"
                 :disabled="remainingCount < 1"
                 color="warning"
          >{{ $t('duplicate_pages.action_auto_delete_remaining', {count: remainingCount}) }}
          </v-btn>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="auto">
          <v-pagination
            v-if="totalPages > 1"
            v-model="page"
            :total-visible="paginationVisible"
            :length="totalPages"
          />
        </v-col>
      </v-row>
    </template>

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

    <confirmation-dialog
      v-model="modalConfirmRemaining"
      :title="$t(`duplicate_pages.action_${dialogConfirmI18n}_remaining`, {count: remainingCount})"
      :body-html="$t(`duplicate_pages.confirm_${dialogConfirmI18n}_remaining`, {count: remainingCount})"
      :button-confirm="$t(`duplicate_pages.action_${dialogConfirmI18n}_remaining`, {count: remainingCount})"
      button-confirm-color="warning"
      @confirm="actionRemaining(confirmAction)"
    />
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {PageHashDto, PageHashUnknownDto} from '@/types/komga-pagehashes'
import {pageHashUnknownThumbnailUrl} from '@/functions/urls'
import PageHashMatchesTable from '@/components/PageHashMatchesTable.vue'
import PageHashUnknownCard from '@/components/PageHashUnknownCard.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {PageHashAction} from '@/types/enum-pagehashes'
import EmptyState from '@/components/EmptyState.vue'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'

export default Vue.extend({
  name: 'DuplicatePagesUnknown',
  components: {ConfirmationDialog, EmptyState, PageHashUnknownCard, PageHashMatchesTable, PageSizeSelect},
  data: function () {
    return {
      elements: [] as PageHashUnknownDto[],
      hiddenElements: [] as PageHashUnknownDto[],
      totalElements: 0,
      page: 1,
      totalPages: 0,
      sortActive: {key: 'totalSize', order: 'desc'} as SortActive,
      dialogImage: false,
      dialogMatches: false,
      dialogImagePageHash: {} as PageHashUnknownDto,
      dialogMatchesPageHash: {} as PageHashDto,
      modalConfirmRemaining: false,
      confirmAction: {} as PageHashAction,
      dialogConfirmI18n: '',
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
    PageHashAction() {
      return PageHashAction
    },
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
    remainingCount(): number {
      return this.elements.length - this.hiddenElements.length
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
      this.hiddenElements = []
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
    confirmRemaining(action: PageHashAction) {
      this.confirmAction = action
      this.dialogConfirmI18n = action === PageHashAction.DELETE_AUTO ? 'auto_delete' : 'manual_delete'
      this.modalConfirmRemaining = true
    },
    async actionRemaining(action: PageHashAction) {
      for (const h of this.elements) {
        if (!this.hiddenElements.includes(h)) {
          this.$komgaPageHashes.createOrUpdatePageHash({
            hash: h.hash,
            size: h.size,
            action: action,
          }).then(() => this.pageHashCreated(h))
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
