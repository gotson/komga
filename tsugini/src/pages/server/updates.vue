<template>
  <q-card v-if="isLoading">
    <q-card-section>
      <q-skeleton type="text" />
      <q-skeleton
        type="text"
        width="200px"
      />
    </q-card-section>

    <q-card-section>
      <q-skeleton
        height="300px"
        square
      />
    </q-card-section>
  </q-card>

  <KEmptyState
    v-else-if="error"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :sub-title="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
    icon="mdi-connection"
    icon-size="250px"
    avatar-color="grey-4"
  />

  <template v-else-if="releases">
    <div>
      <div>
        <div v-if="isLatestVersion == true">
          <KBanner type="positive">
            {{
              $formatMessage({
                description: 'Updates view: banner shown at the top',
                defaultMessage: 'The latest version of Komga is already installed',
                id: 'WNY0pu',
              })
            }}
          </KBanner>
        </div>
        <div v-if="isLatestVersion == false">
          <KBanner type="warning">
            {{
              $formatMessage({
                description: 'Updates view: banner shown at the top',
                defaultMessage: 'Updates are available',
                id: 'n1Ik+L',
              })
            }}
          </KBanner>
        </div>
      </div>
    </div>

    <div
      v-for="(release, index) in releases"
      :key="index"
      class="q-py-sm"
    >
      <q-card>
        <q-card-section>
          <div class="text-h3">
            <a
              :href="release.url"
              target="_blank"
              class="link-underline q-mr-sm"
              >{{ release.version }}</a
            >
            <q-chip
              v-if="release.version == currentVersion"
              class="chip-info"
              :ripple="false"
            >
              {{
                $formatMessage({
                  description:
                    'Updates view: badge showing next to the currently installed release number',
                  defaultMessage: 'Currently installed',
                  id: '3jrAF6',
                })
              }}
            </q-chip>
            <q-chip
              v-if="release.version == latest?.version"
              :ripple="false"
            >
              {{
                $formatMessage({
                  description: 'Updates view: badge showing next to the latest release number',
                  defaultMessage: 'Latest',
                  id: '2Bh8F2',
                })
              }}
            </q-chip>
          </div>
          <div class="text-subtitle1">
            {{ $formatDate(release.releaseDate, { dateStyle: 'long' }) }}
          </div>
        </q-card-section>

        <q-card-section>
          <!-- eslint-disable vue/no-v-html -->
          <div
            class="release"
            v-html="marked(release.description)"
          />
          <!-- eslint-enable vue/no-v-html -->
        </q-card-section>
      </q-card>
    </div>
  </template>
</template>

<script lang="ts" setup>
import { useAppReleases } from 'colada/queries/app-releases'
import { marked } from 'marked'
import { commonMessages } from 'utils/i18n/common-messages'

const {
  data: releases,
  error,
  buildVersion: currentVersion,
  isLatestVersion,
  latestRelease: latest,
  isLoading,
} = useAppReleases()
</script>

<style lang="scss">
.release h2 {
  font-size: 24px;
  line-height: 1.5rem;
}
</style>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
