<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4"
  >
    <v-skeleton-loader
      type="article@6, button@2"
      v-if="isPending"
    />

    <v-empty-state
      v-else-if="error"
      icon="i-mdi:connection"
      :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
      :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
    />
    <template v-else-if="settings">
      <v-card max-width="600px">
        <v-card-text>
          <ServerSettings
            :settings="settings"
            :loading="loading"
            @update-settings="(s) => saveSettings(s)"
          />
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useSettings, useUpdateSettings } from '@/colada/settings'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { components } from '@/generated/openapi/komga'
import { useMessagesStore } from '@/stores/messages'
import type { ErrorCause } from '@/api/komga-client'
import { useIntl } from 'vue-intl'

const intl = useIntl()
const messagesStore = useMessagesStore()

const loading = ref<boolean>(false)

const { data: settings, error, isPending, refetch } = useSettings()
const { mutateAsync } = useUpdateSettings()

function saveSettings(settings: components['schemas']['SettingsUpdateDto']) {
  loading.value = true
  mutateAsync(settings)
    .then(() =>
      messagesStore.messages.push({
        text: intl.formatMessage({
          description: 'Snackbar notification shown upon successful server settings update',
          defaultMessage: 'Settings updated',
          id: 'TL5bVZ',
        }),
      }),
    )
    .catch((error) => {
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
    })
    .finally(() => {
      loading.value = false
      void refetch()
    })
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
