<template>
  <v-skeleton-loader
    v-if="isLoading"
    type="heading, text, paragraph@3"
  />

  <v-empty-state
    v-else-if="error"
    icon="i-mdi:connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

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
</template>

<script lang="ts" setup>
import { useAppReleases } from '@/colada/app-releases'
import { commonMessages } from '@/utils/i18n/common-messages'

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
