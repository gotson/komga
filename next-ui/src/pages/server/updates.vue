<template>
  <v-alert
    v-if="error"
    type="error"
    variant="tonal"
  >
    Error loading data
  </v-alert>

  <template v-if="data">
    <v-row>
      <v-col>
        <div v-if="isLatestVersion == true">
          <v-alert
            type="success"
            variant="tonal"
          >
            The latest version of Komga is already installed
          </v-alert>
        </div>
        <div v-if="isLatestVersion == false">
          <v-alert
            type="warning"
            variant="tonal"
          >
            Updates are available
          </v-alert>
        </div>
      </v-col>
    </v-row>

    <div
      v-for="(release, index) in data"
      :key="index"
    >
      <v-row
        justify="space-between"
        align="center"
      >
        <v-col cols="auto">
          <div>
            <a
              :href="release.url"
              target="_blank"
              class="text-h4 font-weight-medium link-underline me-2"
            >{{
              release.version
            }}</a>
            <v-chip
              v-if="release.version == currentVersion"
              class="mx-2 mt-n3"
              size="small"
              label
              color="info"
            >
              Currently installed
            </v-chip>
            <v-chip
              v-if="release.version == latest?.version"
              class="mx-2 mt-n3"
              size="small"
              label
            >
              Latest
            </v-chip>
          </div>
          <!-- TODO: i18n the date           -->
          <div class="mt-2 subtitle-1">
            {{ release.releaseDate }}
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <!-- eslint-disable vue/no-v-html -->
          <div
            class="release"
            v-html="marked(release.description)"
          />
          <!-- eslint-enable vue/no-v-html -->
        </v-col>
      </v-row>

      <v-divider
        v-if="index != data.length - 1"
        class="my-8"
      />
    </div>
  </template>
</template>

<script lang="ts" setup>
import {useAppReleases} from '@/colada/queries/app-releases.ts'
import {marked} from 'marked'

const {data, error, buildVersion: currentVersion, isLatestVersion, latestRelease: latest} = useAppReleases()
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

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
