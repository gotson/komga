<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col>
          <v-img
            width="200"
            height="300"
            contain
            @click="$emit('image-clicked')"
            :src="pageHashKnownThumbnailUrl(hash)"
            style="cursor: zoom-in"
          />

        </v-col>
        <v-col>
          <v-card-text style="min-width: 200px">
            <v-container>
              <v-row>
                <v-col>
                  <v-chip label small :color="actionColor">
                    {{ $t(`enums.page_hash_action.${hash.action}`) }}
                  </v-chip>
                </v-col>
              </v-row>

              <v-row>
                <v-col>
                  <div>{{ hash.mediaType }}</div>
                  <div>{{ getFileSize(hash.size) || $t('duplicate_pages.unknown_size') }}</div>
                </v-col>
              </v-row>


              <v-row>
                <v-col>
                  <v-btn
                    v-if="matchCount"
                    @click="$emit('matches-clicked')"
                    outlined
                    rounded
                  >
                    {{ $tc('duplicate_pages.matches_n', matchCount) }}
                  </v-btn>
                </v-col>
              </v-row>

              <v-row>
                <v-col>
                  <div
                    v-if="hash.deleteCount"
                  >{{ $t('duplicate_pages.deleted_count', {count: hash.deleteCount}) }}
                  </div>

                  <div
                    v-if="hash.size && hash.deleteCount"
                  >{{ $t('duplicate_pages.saved_size', {size: getFileSize(hash.size * hash.deleteCount)}) }}
                  </div>
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
        </v-col>
      </v-row>
    </v-container>

    <v-card-actions>
      <v-btn v-if="hash.action === PageHashAction.DELETE_MANUAL"
             :color="deleteRequested ? 'success': 'primary'"
             :disabled="!matchCount"
             @click="deleteMatches"
      >
        <v-icon left v-if="deleteRequested">mdi-check</v-icon>
        {{ $t('duplicate_pages.action_delete_matches') }}
      </v-btn>

      <v-btn v-if="hash.action !== PageHashAction.IGNORE" text @click="ignore">{{
          $t('duplicate_pages.action_ignore')
        }}
      </v-btn>
      <v-btn v-if="hash.action !== PageHashAction.DELETE_MANUAL" text @click="deleteManual">
        {{ $t('duplicate_pages.action_delete_manual') }}
      </v-btn>
      <v-btn v-if="hash.action !== PageHashAction.DELETE_AUTO" text @click="deleteAuto" :disabled="!hash.size">
        {{ $t('duplicate_pages.action_delete_auto') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {pageHashKnownThumbnailUrl} from '@/functions/urls'
import {PageHashKnownDto} from '@/types/komga-pagehashes'
import {PageHashAction} from '@/types/enum-pagehashes'
import {getFileSize} from '@/functions/file'

export default Vue.extend({
  name: 'PageHashKnownCard',
  props: {
    hash: {
      type: Object as PropType<PageHashKnownDto>,
    },
  },
  data() {
    return {
      pageHashKnownThumbnailUrl,
      getFileSize,
      PageHashAction,
      matchCount: undefined as number | undefined,
      deleteRequested: false,
    }
  },
  computed: {
    actionColor(): string {
      switch (this.hash.action) {
        case PageHashAction.DELETE_AUTO:
          return 'success'
        case PageHashAction.DELETE_MANUAL:
          return 'warning'
        default:
          return 'grey'
      }
    },
  },
  mounted() {
    this.getMatchCount()
  },
  watch: {
    hash: {
      handler() {
        this.deleteRequested = false
        this.matchCount = undefined
        this.getMatchCount()
      },
      deep: true,
    },
  },
  methods: {
    async getMatchCount() {
      if (this.hash?.action === PageHashAction.DELETE_MANUAL)
        this.matchCount = (await this.$komgaPageHashes.getPageHashMatches(this.hash, {size: 0})).totalElements
      else
        this.matchCount = undefined
    },
    async deleteMatches() {
      if(!this.deleteRequested) {
        await this.$komgaPageHashes.deleteAllMatches(this.hash)
        this.deleteRequested = true
      }
    },
    ignore() {
      this.updatePageHash(PageHashAction.IGNORE)
    },
    deleteManual() {
      this.updatePageHash(PageHashAction.DELETE_MANUAL)
    },
    deleteAuto() {
      this.updatePageHash(PageHashAction.DELETE_AUTO)
    },
    async updatePageHash(action: PageHashAction) {
      try {
        const p = {
          hash: this.hash.hash,
          mediaType: this.hash.mediaType,
          size: this.hash.size,
          action: action,
        }
        await this.$komgaPageHashes.createOrUpdatePageHash(p)
        this.$emit('updated', p)
      } catch (e) {
      }
    },
  },
})
</script>
