<template>
  <v-card>
    <template #title>
      <div>
        <a
          :href="release.url"
          target="_blank"
          class="text-h4 font-weight-medium link-underline me-2"
          >{{ release.version }}</a
        >
        <span class="d-inline-flex mt-n3 ga-2 ms-2">
          <v-chip
            v-if="current"
            size="small"
            rounded
            color="info"
          >
            {{
              $formatMessage({
                description:
                  'Updates view: badge showing next to the currently installed release number',
                defaultMessage: 'Installed',
                id: 'WADecv',
              })
            }}
          </v-chip>
          <v-chip
            v-if="latest"
            size="small"
            rounded
          >
            {{
              $formatMessage({
                description: 'Updates view: badge showing next to the latest release number',
                defaultMessage: 'Latest',
                id: '2Bh8F2',
              })
            }}
          </v-chip>
        </span>
      </div>
    </template>

    <template #subtitle>
      {{ $formatDate(release.releaseDate, { dateStyle: 'long' }) }}
    </template>

    <template #text>
      <!-- eslint-disable vue/no-v-html -->
      <div
        class="release"
        v-html="marked(release.description)"
      />
      <!-- eslint-enable vue/no-v-html -->
    </template>
  </v-card>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { marked } from 'marked'

const {
  release,
  latest = false,
  current = false,
} = defineProps<{
  release: components['schemas']['ReleaseDto']
  latest?: boolean
  current?: boolean
}>()
</script>

<style lang="scss">
.release p {
  margin-bottom: 16px;
}

.release ul {
  padding-left: 24px;
}

.release a {
  color: var(--v-anchor-base);
}
</style>
