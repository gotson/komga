<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4"
  >
    <v-skeleton-loader
      v-if="isPending"
      type="article@6, button@2"
    />

    <EmptyStateNetworkError v-else-if="error" />

    <template v-else-if="settings">
      <v-card max-width="600px">
        <v-card-text>
          <ServerSettings
            :settings="effectiveSettings"
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

import { useMessagesStore } from '@/stores/messages'
import { komgaBooksRegenerateThumbnails } from '@/generated/openapi'
import { useMutation } from '@pinia/colada'
import type { SettingsUpdateDtoExtended, ThumbnailRegenerate } from '@/types/Settings'
import { useClientSettingsGlobal, useUpdateClientSettingsGlobal } from '@/colada/client-settings'
import { pick } from '@/functions/pick'
import type { ClientSettingGlobalSettings } from '@/types/ClientSettingsGlobal'

const messagesStore = useMessagesStore()

const loading = ref<boolean>(false)

const { data: settings, error, isPending } = useSettings()
const { globalSettings } = useClientSettingsGlobal()

const effectiveSettings = computed(() => ({
  ...settings.value,
  oauth2: globalSettings.value['komga.nextui.oauth2'].value,
}))

const { mutateAsync: mutateServerSettings } = useUpdateSettings()
const { mutateAsync: mutateGlobalSettings } = useUpdateClientSettingsGlobal()

function saveSettings(settings: SettingsUpdateDtoExtended) {
  loading.value = true

  const globalUpdate = {
    'komga.nextui.oauth2': {
      allowUnauthorized: true,
      value: pick(settings, 'oauth2').oauth2,
    },
  } satisfies ClientSettingGlobalSettings

  void mutateGlobalSettings(globalUpdate)

  mutateServerSettings(settings)
    .then(() => {
      messagesStore.messages.push({
        description: 'Snackbar notification shown upon successful server settings update',
        defaultMessage: 'Settings updated',
        id: 'TL5bVZ',
      })

      regenerateThumbnails(settings.thumbnailRegenerate)
    })
    .catch((error) => {
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
    })
    .finally(() => {
      loading.value = false
    })
}

function regenerateThumbnails(regenerate: ThumbnailRegenerate) {
  if (regenerate === 'no') return
  const { mutate } = useMutation({
    mutation: () =>
      komgaBooksRegenerateThumbnails({
        query: {
          for_bigger_result_only: regenerate === 'bigger',
        },
      }),
  })
  void mutate()
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
