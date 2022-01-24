<template>
  <v-card>
    <v-img
      :src="getImage(item)"
      aspect-ratio="0.7071"
      contain/>
    <v-card-actions align="center">
      <v-tooltip top>
        <template v-slot:activator="{ on, attrs }">
          <v-icon
            class="v-btn--icon v-size--default px-2"
            :color="isFileToBig(item) ? 'error' : ''"
            v-bind="attrs"
            v-on="on">
            {{ getStatusIcon(item) }}
          </v-icon>
        </template>
        <span>{{ getStatusTooltip(item) }}</span>
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
        <span>{{
            selected ? $t('thumbnail_card.tooltip_selected') : $t('thumbnail_card.tooltip_mark_as_selected')
          }}</span>
      </v-tooltip>

      <v-tooltip v-if="isDeletable(item)" top>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            :color="toBeDeleted ? 'error' : ''"
            @click="onClickDelete"
            v-bind="attrs"
            v-on="on">
            <v-icon>mdi-trash-can-outline</v-icon>
          </v-btn>
        </template>
        <span>{{
            toBeDeleted ? $t('thumbnail_card.tooltip_to_be_deleted') : $t('thumbnail_card.tooltip_delete')
          }}</span>
      </v-tooltip>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue from 'vue'
import {SeriesThumbnailDto} from '@/types/komga-series'
import {
  bookThumbnailUrlByThumbnailId,
  collectionThumbnailUrlByThumbnailId,
  readListThumbnailUrlByThumbnailId,
  seriesThumbnailUrlByThumbnailId,
} from '@/functions/urls'
import {BookThumbnailDto} from '@/types/komga-books'

export default Vue.extend({
  name: 'ThumbnailCard',
  props: {
    item: {
      required: true,
      validator: (value: any) => {
        if (value instanceof File) {
          return true
        }
        return 'id' in value && 'type' in value && 'selected' in value && ('seriesId' in value || 'bookId' in value || 'readListId' in value || 'collectionId' in value)
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
    getStatusIcon(item: File | SeriesThumbnailDto | BookThumbnailDto | ReadListThumbnailDto | CollectionThumbnailDto): string {
      if (item instanceof File) {
        if (this.isFileToBig(item)) {
          return 'mdi-alert-circle'
        } else {
          return 'mdi-cloud-upload-outline'
        }
      } else {
        if (item.type === 'SIDECAR') {
          return 'mdi-folder-outline'
        } else if (item.type === 'GENERATED') {
          return 'mdi-file-outline'
        } else {
          return 'mdi-cloud-check-outline'
        }
      }
    },
    getStatusTooltip(item: File | SeriesThumbnailDto | BookThumbnailDto | ReadListThumbnailDto | CollectionThumbnailDto): string {
      if (item instanceof File) {
        if (this.isFileToBig(item)) {
          return this.$t('thumbnail_card.tooltip_too_big').toString()
        } else {
          return this.$t('thumbnail_card.tooltip_to_be_uploaded').toString()
        }
      } else {
        if (item.type === 'SIDECAR') {
          return this.$t('thumbnail_card.tooltip_sidecar').toString()
        }
        if (item.type === 'GENERATED') {
          return this.$t('thumbnail_card.tooltip_generated').toString()
        } else {
          return this.$t('thumbnail_card.tooltip_user_uploaded').toString()
        }
      }
    },
    isFileToBig(item: File | SeriesThumbnailDto | BookThumbnailDto | ReadListThumbnailDto | CollectionThumbnailDto): boolean {
      if (item instanceof File) {
        return item.size > 1_000_000
      } else {
        return false
      }
    },
    getImage(item: File | SeriesThumbnailDto | BookThumbnailDto | ReadListThumbnailDto | CollectionThumbnailDto): string {
      if (item instanceof File) {
        return URL.createObjectURL(item)
      } else if ('seriesId' in item) {
        return seriesThumbnailUrlByThumbnailId(item.seriesId, item.id)
      } else if ('bookId' in item) {
        return bookThumbnailUrlByThumbnailId(item.bookId, item.id)
      } else if ('readListId' in item) {
        return readListThumbnailUrlByThumbnailId(item.readListId, item.id)
      } else if ('collectionId' in item) {
        return collectionThumbnailUrlByThumbnailId(item.collectionId, item.id)
      } else {
        throw new Error('The given item type is not known!')
      }
    },
    onClickSelect() {
      if (!this.selected) {
        this.$emit('on-select-thumbnail', this.item)
      }
    },
    isDeletable(item: File | SeriesThumbnailDto | BookThumbnailDto | ReadListThumbnailDto | CollectionThumbnailDto) {
      if (item instanceof File) {
        return true
      } else {
        return item.type !== 'SIDECAR' && item.type !== 'GENERATED'
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
