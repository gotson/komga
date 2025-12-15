<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <EmptyStateNetworkError v-if="error" />

    <template v-else>
      <ApikeyTable
        :api-keys="apiKeys"
        :loading="isLoading"
        @enter-add-api-key="(target) => (dialogGenerateActivator = target)"
        @enter-force-sync-api-key="(target) => (dialogConfirm.activator = target)"
        @force-sync-api-key="(apiKey) => showDialog(ACTION.FORCE_SYNC, apiKey)"
        @enter-delete-api-key="(target) => (dialogConfirm.activator = target)"
        @delete-api-key="(apiKey) => showDialog(ACTION.DELETE, apiKey)"
      />
    </template>

    <ApikeyGenerateDialog
      :activator="dialogGenerateActivator"
      :fullscreen="display.xs.value"
    />
  </v-container>
</template>

<script lang="ts" setup>
import { type ErrorCause } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { commonMessages } from '@/utils/i18n/common-messages'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import ApikeyDeletionWarning from '@/components/apikey/DeletionWarning.vue'
import ForceSyncWarning from '@/components/apikey/ForceSyncWarning.vue'
import { useApiKeys, useDeleteApiKey } from '@/colada/users'
import { useDeleteSyncPoints } from '@/colada/syncpoints'
import { useDisplay } from 'vuetify'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'

const intl = useIntl()
const display = useDisplay()

// API data
const { data: apiKeys, error, isLoading, refetch: refetchApiKeys } = useApiKeys()

onMounted(() => refetchApiKeys())

// Dialogs handling
// stores the API Key being actioned upon
const apiKeyRecord = ref<components['schemas']['ApiKeyDto']>()
// stores the ongoing action, so we can handle the action when the dialog is closed with changes
const currentAction = ref<ACTION>()
const dialogGenerateActivator = ref<Element | undefined>(undefined)

const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())

const { mutateAsync: mutateDeleteApiKey } = useDeleteApiKey()
const { mutateAsync: mutateDeleteSyncPoints } = useDeleteSyncPoints()

const messagesStore = useMessagesStore()

enum ACTION {
  DELETE,
  FORCE_SYNC,
}

function showDialog(action: ACTION, apiKey?: components['schemas']['ApiKeyDto']) {
  currentAction.value = action
  switch (action) {
    case ACTION.DELETE:
      dialogConfirm.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Delete API Key dialog title',
          defaultMessage: 'Delete API Key',
          id: '3beD4X',
        }),
        subtitle: apiKey?.comment,
        maxWidth: 600,
        validateText: apiKey?.comment,
        mode: 'textinput',
        okText: intl.formatMessage({
          description: 'Delete API Key dialog: confirmation button text',
          defaultMessage: 'Delete',
          id: 'IE0XzE',
        }),
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirm.value.slotWarning = {
        component: markRaw(ApikeyDeletionWarning),
        props: {},
      }
      dialogConfirm.value.callback = handleDialogConfirmation
      break
    case ACTION.FORCE_SYNC:
      dialogConfirm.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Force Sync API Key dialog title',
          defaultMessage: 'Force Kobo sync',
          id: '/lE31l',
        }),
        subtitle: apiKey?.comment,
        maxWidth: 600,
        validateText: apiKey?.comment,
        mode: 'textinput',
        okText: intl.formatMessage({
          description: 'Force Sync API Key dialog: confirmation button text',
          defaultMessage: 'I understand',
          id: 'W3BUf7',
        }),
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirm.value.slotWarning = {
        component: markRaw(ForceSyncWarning),
        props: {},
      }
      dialogConfirm.value.callback = handleDialogConfirmation
  }
  apiKeyRecord.value = apiKey
}

function handleDialogConfirmation(
  hideDialog: () => void,
  setLoading: (isLoading: boolean) => void,
) {
  let mutation: Promise<unknown> | undefined
  let successMessage: string | undefined

  setLoading(true)

  switch (currentAction.value) {
    case ACTION.DELETE:
      mutation = mutateDeleteApiKey(apiKeyRecord.value!.id)
      successMessage = intl.formatMessage(
        {
          description: 'Snackbar notification shown upon successful API key deletion',
          defaultMessage: 'API key deleted: {apiKeyComment}',
          id: 'NArFTo',
        },
        {
          apiKeyComment: apiKeyRecord.value!.comment,
        },
      )
      break
    case ACTION.FORCE_SYNC:
      mutation = mutateDeleteSyncPoints([apiKeyRecord.value!.id])
      successMessage = intl.formatMessage(
        {
          description: 'Snackbar notification shown upon successful API key force sync',
          defaultMessage: 'Kobo sync forced: {apiKeyComment}',
          id: 'NN7kAK',
        },
        {
          apiKeyComment: apiKeyRecord.value!.comment,
        },
      )
      break
  }

  mutation
    ?.then(() => {
      hideDialog()
      if (successMessage) messagesStore.messages.push({ text: successMessage })
    })
    .catch((error) => {
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
      setLoading(false)
    })
}
</script>
