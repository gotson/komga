<template>
  <v-container fluid>
    <v-skeleton-loader
      v-if="isLoading"
      type="heading, text, paragraph@3"
    />

    <EmptyStateNetworkError v-else-if="error" />

    <template v-else-if="releases">
      <v-row>
        <v-col>
          <div v-if="isLatestVersion == true">
            <v-alert
              type="success"
              variant="tonal"
            >
              {{
                $formatMessage({
                  description: 'Updates view: banner shown at the top',
                  defaultMessage: 'The latest version of Komga is already installed',
                  id: 'WNY0pu',
                })
              }}
            </v-alert>
          </div>
          <div v-if="isLatestVersion == false">
            <v-alert
              type="warning"
              variant="tonal"
            >
              {{
                $formatMessage({
                  description: 'Updates view: banner shown at the top',
                  defaultMessage: 'Updates are available',
                  id: 'n1Ik+L',
                })
              }}
            </v-alert>
          </div>
        </v-col>
      </v-row>

      <div
        v-for="(release, index) in releases"
        :key="index"
      >
        <ReleaseCard
          :release="release"
          :current="release.version == currentVersion"
          :latest="release.version == latest?.version"
          class="my-4"
        />
      </div>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useAppReleases } from '@/colada/app-releases'
import { commonMessages } from '@/utils/i18n/common-messages'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'

const {
  data: releases,
  error,
  buildVersion: currentVersion,
  isLatestVersion,
  latestRelease: latest,
  isLoading,
} = useAppReleases()
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
