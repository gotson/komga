<template>
  <div class="d-flex flex-column flex-md-row align-center ga-2">
    <div style="width: 180px; height: 180px">
      <v-img
        width="180"
        height="180"
        contain
        :src="page.url"
        lazy-src="@/assets/cover.svg"
      >
        <template #placeholder>
          <div class="d-flex align-center justify-center fill-height">
            <v-progress-circular
              color="grey"
              indeterminate
            />
          </div>
        </template>
      </v-img>
    </div>
    <div class="d-flex flex-column ga-2">
      <div style="overflow-wrap: anywhere">{{ page.fileName }}</div>
      <div>
        {{
          $formatMessage(
            {
              description: 'Page comparison: file dimensions',
              defaultMessage: 'w: {width}, h: {height}',
              id: 'yyAGH1',
            },
            {
              width: page.width,
              height: page.height,
            },
          )
        }}
      </div>
      <div>{{ getFileSize(page.sizeBytes) }}</div>
      <v-chip
        rounded
        size="small"
        :text="page.mediaType"
        :color="getMediaTypeColor(page.mediaType)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getFileSize } from '@/functions/filesize'
import { VImg } from 'vuetify/components'
import type { PageDtoWithUrl } from '@/types/BookDetails'
import { getMediaTypeColor } from '@/types/image'

defineProps<{
  page: PageDtoWithUrl
}>()
</script>
