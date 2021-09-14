<template>
  <v-card>
    <v-img
      :src="getImage(item)"
      aspect-ratio="0.7071"
      contain />
    <v-card-actions align="center">
      <v-btn
        icon
        disabled>
        <v-icon>
          {{ getIcon(item) }}
        </v-icon>
      </v-btn>
      <v-btn
        icon
        :color="selected ? 'success' : ''"
        @click="onClickSelect"
      >
        <v-icon>mdi-check</v-icon>
      </v-btn>
      <v-btn
        icon
        v-if="isDeletable(item)"
        :color="toBeDeleted ? 'error' : ''"
        @click="onClickDelete"
      >
        <v-icon>mdi-trash-can-outline</v-icon>
      </v-btn>
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
      type:  Object as () => File | SeriesThumbnailDto,
      required: true,
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
        return 'mdi-cloud-upload-outline'
      } else {
        return item.type === 'SIDECAR' ? 'mdi-folder-outline' : 'mdi-cloud-check-outline'
      }
    },
    getImage(item: File | SeriesThumbnailDto): string {
      if (item instanceof File) {
        return this.extractImageFromFile(item)
      } else {
        return this.getThumbnailById(item)
      }
    },
    extractImageFromFile: (file: File): string => URL.createObjectURL(file),
    getThumbnailById: (thumbnail: SeriesThumbnailDto): string => seriesThumbnailUrlByThumbnailId(thumbnail.seriesId, thumbnail.id),
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
