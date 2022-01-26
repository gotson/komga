<template>
  <v-container fluid class="pa-6">
    <v-pagination
      v-if="totalPages > 1"
      v-model="page"
      :total-visible="paginationVisible"
      :length="totalPages"
      class="mb-2"
    />

    <v-row>
      <v-card
        v-for="(element, i) in elements"
        :key="i"
        class="ma-2"
      >
        <v-container>
          <v-row>
            <v-col>
              <v-img
                width="200"
                contain
                @click="showDialogImage(element.hash)"
                :src="pageHashUnknownThumbnailUrl(element.hash, 500)"
                style="cursor: zoom-in"
              />

            </v-col>
            <v-col>
              <v-card-text>
                <div>{{ element.mediaType }}</div>
                <div>{{ element.size || $t('duplicate_pages.unknown_size') }}</div>
                <v-btn
                  @click="showDialogMatches(element.hash)"
                  outlined
                  rounded
                  class="mt-2"
                >
                  {{ $tc('duplicate_pages.matches_n', element.matchCount) }}
                </v-btn>
              </v-card-text>
            </v-col>
          </v-row>

          <v-card-actions>
            <v-btn text disabled>{{ $t('duplicate_pages.action_ignore') }}</v-btn>
            <v-btn text disabled>{{ $t('duplicate_pages.action_delete_manual') }}</v-btn>
            <v-btn text disabled>{{ $t('duplicate_pages.action_delete_auto') }}</v-btn>
          </v-card-actions>
        </v-container>
      </v-card>

    </v-row>

    <v-dialog v-model="dialogImage">
      <v-card>
        <v-card-text>
          <v-img
            @click="dialogImage = false"
            contain
            :src="pageHashUnknownThumbnailUrl(dialogImageHash)"
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
            :hash="dialogMatchesHash"
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
import {PageHashUnknownDto} from '@/types/komga-pagehashes'
import {pageHashUnknownThumbnailUrl} from '@/functions/urls'
import PageHashMatchesTable from '@/components/PageHashMatchesTable.vue'

export default Vue.extend({
  name: 'SettingsDuplicatePages',
  components: {PageHashMatchesTable},
  data: function () {
    return {
      elements: [] as PageHashUnknownDto[],
      totalElements: 0,
      page: 1,
      totalPages: 1,
      dialogImage: false,
      dialogMatches: false,
      dialogImageHash: '',
      dialogMatchesHash: '',
      pageHashUnknownThumbnailUrl,
    }
  },
  async mounted() {
    await this.loadData(this.page)
  },
  watch: {
    page(val) {
      this.loadData(val)
    },
  },
  computed: {
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
    async loadData(page: number) {
      const pageRequest = {
        page: page - 1,
        sort: ['matchCount,desc'],
      } as PageRequest

      const itemsPage = await this.$komgaPageHashes.getUnknownHashes(pageRequest)
      this.totalElements = itemsPage.totalElements
      this.totalPages = itemsPage.totalPages
      this.elements = itemsPage.content
    },
    showDialogImage(hash: string) {
      this.dialogImageHash = hash
      this.dialogImage = true
    },
    showDialogMatches(hash: string) {
      this.dialogMatchesHash = hash
      this.dialogMatches = true
    },
  },
})
</script>

<style scoped>

</style>
