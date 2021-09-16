<template>
  <v-card>
    <v-img
      :src="getImage(item)"
      aspect-ratio="0.7071"
      contain />
    <v-card-actions align="center">
      <v-tooltip top>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            :color="getColor(item)"
            v-bind="attrs"
            v-on="on">
            <v-icon>
              {{ getIcon(item) }}
            </v-icon>
          </v-btn>
        </template>
        <span>{{ getTooltip(item) }}</span>
      </v-tooltip>

      <v-tooltip v-if="!isFileToBig(item)" top>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            :color="selected ? 'success' : ''"
            @click="onClickSelect"
            v-bind="attrs"
            v-on="on">
            <v-icon>mdi-check</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('thumbnail_card.tooltip_mark_as_selected') }}</span>
      </v-tooltip>

      <v-tooltip top>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            v-if="isDeletable(item)"
            :color="toBeDeleted ? 'error' : ''"
            @click="onClickDelete"
            v-bind="attrs"
            v-on="on">
            <v-icon>mdi-trash-can-outline</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('thumbnail_card.tooltip_delete') }}</span>
      </v-tooltip>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue from 'vue'
import {SeriesThumbnailDto} from '@/types/komga-series'
import {seriesThumbnailUrlByThumbnailId} from '@/functions/urls'

export default Vue.extend({
  name: 'ThumbnailCard',
  props: {
    item: {
      required: true,
      validator: (value: any) => {
        if (value instanceof File) {
          return true
        }
        return 'id' in value && 'seriesId' in value && 'type' in value && 'selected' in value
      },
    },
    selected: {
      type: Boolean,
      required: true,
    },
    toBeDeleted: {
      type: Boolean,
      required: true,
    },
  },
  methods: {
    getIcon(item: File | SeriesThumbnailDto): string {
      if (item instanceof File) {
        if (this.isFileToBig(item)) {
          return 'mdi-alert-circle'
        } else {
          return 'mdi-cloud-upload-outline'
        }
      } else {
        if (item.type === 'SIDECAR') {
          return 'mdi-folder-outline'
        } else {
          return 'mdi-cloud-check-outline'
        }
      }
    },
    getColor(item: File | SeriesThumbnailDto): string {
      return item instanceof File && this.isFileToBig(item) ? 'error' : ''
    },
    getTooltip(item: File | SeriesThumbnailDto): string {
      if (item instanceof File) {
        if (this.isFileToBig(item)) {
          return this.$tc('thumbnail_card.tooltip_to_big')
        } else {
          return this.$tc('thumbnail_card.tooltip_to_be_uploaded')
        }
      } else {
        if (item.type === 'SIDECAR') {
          return this.$tc('thumbnail_card.tooltip_sidecar')
        } else {
          return this.$tc('thumbnail_card.tooltip_user_uploaded')
        }
      }
    },
    isFileToBig(item: File | SeriesThumbnailDto): boolean {
      if (item instanceof File) {
        return item.size > 1_000_000
      } else {
        return false
      }
    },
    getImage(item: File | SeriesThumbnailDto): string {
      if (item instanceof File) {
        return URL.createObjectURL(item)
      } else {
        return seriesThumbnailUrlByThumbnailId(item.seriesId, item.id)
      }
    },
    onClickSelect() {
      if (!this.selected) {
        this.$emit('on-select-thumbnail', this.item)
      }
    },
    isDeletable(item: File | SeriesThumbnailDto) {
      if (item instanceof File) {
        return true
      } else {
        return item.type !== 'SIDECAR'
      }
    },
    onClickDelete() {
      this.$emit('on-delete-thumbnail', this.item)
    },
  },
})
</script>

<style scoped>

</style>
