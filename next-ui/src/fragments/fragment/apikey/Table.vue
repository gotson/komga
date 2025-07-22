<template>
  <v-data-table
    :loading="loading"
    :items="apiKeys"
    :headers="headers"
    :hide-default-footer="hideFooter"
    mobile-breakpoint="md"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title>
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:key-chain-variant"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'API Key table global header',
              defaultMessage: 'API Keys',
              id: 'yDWzUi',
            })
          }}
        </v-toolbar-title>

        <v-btn
          class="me-2"
          prepend-icon="i-mdi:plus"
          rounded="lg"
          :text="
            $formatMessage({
              description: 'API Key table global header: generate API key button',
              defaultMessage: 'Generate API Key',
              id: '6MkTnp',
            })
          "
          border
          :disabled="loading"
          @click="emit('addApiKey')"
          @mouseenter="emit('enterAddApiKey', $event.currentTarget)"
        />
      </v-toolbar>
    </template>

    <template #no-data>{{
      $formatMessage({
        description: 'API Key table: shown when table has no data',
        defaultMessage: 'No API keys created yet',
        id: 'WLwQG8',
      })
    }}</template>

    <template #[`item.createdDate`]="{ value }">
      {{ $formatDate(value, { dateStyle: 'medium', timeStyle: 'short' }) }}
    </template>

    <template #[`item.activity`]="{ value }">
      <template v-if="value"
        >{{ $formatDate(value, { dateStyle: 'medium', timeStyle: 'short' }) }}
      </template>
      <template v-else
        >{{
          $formatMessage({
            description:
              'Shown in API key table when there is no recent authentication activity for the key',
            defaultMessage: 'No recent activity',
            id: 'OW1/zn',
          })
        }}
      </template>
    </template>

    <template #[`item.actions`]="{ item: apiKey }">
      <div class="d-flex ga-1 justify-end">
        <v-icon-btn
          v-tooltip:bottom="$formatMessage(messages.forceKoboSync)"
          icon="i-mdi:sync-alert"
          @click="emit('forceSyncApiKey', apiKey)"
          @mouseenter="emit('enterForceSyncApiKey', $event.currentTarget)"
          :aria-label="$formatMessage(messages.forceKoboSync)"
        />
        <v-icon-btn
          v-tooltip:bottom="$formatMessage(messages.deleteApiKey)"
          icon="i-mdi:delete"
          @click="emit('deleteApiKey', apiKey)"
          @mouseenter="emit('enterDeleteApiKey', $event.currentTarget)"
          :aria-label="$formatMessage(messages.deleteApiKey)"
        />
      </div>
    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { defineMessage, useIntl } from 'vue-intl'
import { komgaClient } from '@/api/komga-client'

const intl = useIntl()

const { apiKeys = [], loading = false } = defineProps<{
  apiKeys?: components['schemas']['ApiKeyDto'][]
  loading?: boolean
}>()

const emit = defineEmits<{
  addApiKey: []
  enterAddApiKey: [target: Element]
  enterForceSyncApiKey: [target: Element]
  enterDeleteApiKey: [target: Element]
  forceSyncApiKey: [apiKey: components['schemas']['ApiKeyDto']]
  deleteApiKey: [apiKey: components['schemas']['ApiKeyDto']]
}>()

const hideFooter = computed(() => apiKeys.length < 11)
const headers = [
  {
    title: intl.formatMessage({
      description: 'API Key table header: key comment',
      defaultMessage: 'Comment',
      id: 'gNiEAF',
    }),
    key: 'comment',
  },
  {
    title: intl.formatMessage({
      description: 'API Key table header: key creation date',
      defaultMessage: 'Creation date',
      id: 'mP9Ldq',
    }),
    key: 'createdDate',
  },
  {
    title: intl.formatMessage({
      description: 'API Key table header: key latest activity',
      defaultMessage: 'Latest activity',
      id: 'hgiBeR',
    }),
    key: 'activity',
    value: (item: components['schemas']['ApiKeyDto']) => latestActivity[item.id],
  },
  {
    title: intl.formatMessage({
      description: 'API Key table header: key actions',
      defaultMessage: 'Actions',
      id: 'rKyTwd',
    }),
    key: 'actions',
    align: 'end',
    sortable: false,
  },
] as const // workaround for https://github.com/vuetifyjs/vuetify/issues/18901

// store each key's latest activity in a map
// when the 'apiKeys' change, we call the API for each key
const latestActivity: Record<string, Date | undefined> = reactive({})

function getLatestActivity(key: components['schemas']['ApiKeyDto']) {
  komgaClient
    .GET('/api/v2/users/{id}/authentication-activity/latest', {
      params: {
        path: { id: key.userId },
        query: { apikey_id: key.id },
      },
    })
    // unwrap the openapi-fetch structure on success
    .then((res) => (latestActivity[key.id] = res.data?.dateTime))
    .catch(() => {})
}

watch(
  () => apiKeys,
  (apiKeys) => {
    if (apiKeys)
      for (const key of apiKeys) {
        getLatestActivity(key)
      }
  },
  { immediate: true },
)

const messages = {
  deleteApiKey: defineMessage({
    description: 'Tooltip for the delete API key button in the API Key table',
    defaultMessage: 'Delete API Key',
    id: 'hude41',
  }),
  forceKoboSync: defineMessage({
    description: 'Tooltip for the force Kobo sync button in the API Key table',
    defaultMessage: 'Force Kobo sync',
    id: 't0Tkmy',
  }),
}
</script>
