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
            :src="pageHashUnknownThumbnailUrl(hash, 500)"
            style="cursor: zoom-in"
          />

        </v-col>
        <v-col>
          <v-card-text style="min-width: 200px">
            <v-container>
              <v-row>
                <v-col>
                  <div>{{ getFileSize(hash.size) || $t('duplicate_pages.unknown_size') }}</div>
                </v-col>
              </v-row>

              <v-row>
                <v-col>
                  <v-btn
                    @click="$emit('matches-clicked')"
                    outlined
                    rounded
                  >
                    {{ $tc('duplicate_pages.matches_n', hash.matchCount) }}
                  </v-btn>
                </v-col>
              </v-row>

              <v-row>
                <v-col>
                  <div
                    v-if="hash.size"
                  >{{ $t('duplicate_pages.delete_to_save', {size: getFileSize(hash.size * hash.matchCount)}) }}
                  </div>
                </v-col>
              </v-row>

            </v-container>
          </v-card-text>
        </v-col>
      </v-row>
    </v-container>

    <v-card-actions>
      <v-btn text @click="ignore">{{ $t('duplicate_pages.action_ignore') }}</v-btn>
      <v-btn text @click="deleteManual">{{ $t('duplicate_pages.action_delete_manual') }}</v-btn>
      <v-btn text @click="deleteAuto" :disabled="!hash.size">{{ $t('duplicate_pages.action_delete_auto') }}</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {pageHashUnknownThumbnailUrl} from '@/functions/urls'
import {PageHashUnknownDto} from '@/types/komga-pagehashes'
import {PageHashAction} from '@/types/enum-pagehashes'
import {getFileSize} from '@/functions/file'

export default Vue.extend({
  name: 'PageHashUnknownCard',
  props: {
    hash: {
      type: Object as PropType<PageHashUnknownDto>,
    },
  },
  data() {
    return {
      pageHashUnknownThumbnailUrl,
      getFileSize,
    }
  },
  methods: {
    ignore() {
      this.createPageHash(PageHashAction.IGNORE)
    },
    deleteManual() {
      this.createPageHash(PageHashAction.DELETE_MANUAL)
    },
    deleteAuto() {
      this.createPageHash(PageHashAction.DELETE_AUTO)
    },
    async createPageHash(action: PageHashAction) {
      try {
        await this.$komgaPageHashes.createOrUpdatePageHash({
          hash: this.hash.hash,
          size: this.hash.size,
          action: action,
        })
        this.$emit('created')
      } catch (e) {
      }
    },
  },
})
</script>
