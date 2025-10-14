<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <v-file-upload
      v-model="fileToUpload"
      :disabled="isLoading"
      density="compact"
      filter-by-type=".cbl"
      @rejected="handleReject()"
    >
      <template #item />
    </v-file-upload>

    <v-progress-linear
      v-if="isLoading"
      indeterminate
      class="mt-2"
    />

    <ImportReadlistTable
      v-if="match"
      :match="match"
      :loading="isLoading"
      class="mt-4"
    />
  </v-container>
</template>

<script lang="ts" setup>
import { useMessagesStore } from '@/stores/messages'
import { useMutation } from '@pinia/colada'
import { type ErrorCause, komgaClient } from '@/api/komga-client'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useIntl } from 'vue-intl'

const intl = useIntl()
const messagesStore = useMessagesStore()
const { convertErrorCodes } = useErrorCodeFormatter()

const fileToUpload = ref<File>()

function handleReject() {
  messagesStore.messages.push(unsupportedFileTypeMessage)
}

const {
  data: match,
  mutate: matchCbl,
  isLoading,
} = useMutation({
  mutation: (file: File) =>
    komgaClient
      .POST('/api/v1/readlists/match/comicrack', {
        body: {
          file: file,
        },
        bodySerializer() {
          const fd = new FormData()
          fd.append('file', file)
          return fd
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data)
      .catch((error) => {
        messagesStore.messages.push({
          text:
            convertErrorCodes((error?.cause as ErrorCause)?.message) ||
            intl.formatMessage(commonMessages.networkError),
        })
      }),
})

watch(fileToUpload, (file) => {
  if (file) matchCbl(file)
})

const unsupportedFileTypeMessage = intl.formatMessage({
  description: 'Import readlist view: error message when trying to upload an unsupported file type',
  defaultMessage: 'File type not supported',
  id: 'CxuwFR',
})
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
