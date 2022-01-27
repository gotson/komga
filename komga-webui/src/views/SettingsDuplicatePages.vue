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
        <v-container fluid>
          <v-row>
            <v-col>
              <v-img
                width="200"
                height="300"
                contain
                @click="showDialogImage(element)"
                :src="pageHashUnknownThumbnailUrl(element, 500)"
                style="cursor: zoom-in"
              />

            </v-col>
            <v-col>
              <v-card-text>
                <div>{{ element.mediaType }}</div>
                <div>{{ element.size || $t('duplicate_pages.unknown_size') }}</div>

                <v-btn
                  @click="showDialogMatches(element)"
                  outlined
                  rounded
                  class="my-4"
                >
                  {{ $tc('duplicate_pages.matches_n', element.matchCount) }}
                </v-btn>

                <div
                  v-if="element.totalSize"
                  style="max-width: 100px"
                >{{ $t('duplicate_pages.delete_to_save', {size: element.totalSize}) }}
                </div>
              </v-card-text>
            </v-col>
          </v-row>
        </v-container>

        <v-card-actions>
          <v-btn text disabled>{{ $t('duplicate_pages.action_ignore') }}</v-btn>
          <v-btn text disabled>{{ $t('duplicate_pages.action_delete_manual') }}</v-btn>
          <v-btn text disabled>{{ $t('duplicate_pages.action_delete_auto') }}</v-btn>
        </v-card-actions>
      </v-card>

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
      dialogImagePageHash: {} as PageHashUnknownDto,
      dialogMatchesPageHash: {} as PageHashUnknownDto,
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
    showDialogImage(pageHash: PageHashUnknownDto) {
      this.dialogImagePageHash = pageHash
      this.dialogImage = true
    },
    showDialogMatches(pageHash: PageHashUnknownDto) {
      this.dialogMatchesPageHash = pageHash
      this.dialogMatches = true
    },
  },
})
</script>

<style scoped>

</style>
