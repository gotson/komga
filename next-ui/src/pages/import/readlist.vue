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
      :error-messages="errorMessage"
      @rejected="handleReject()"
    >
    </v-file-upload>

    <v-progress-linear
      v-if="isLoading"
      indeterminate
    />

    <ImportReadlistTable
      v-if="match"
      :match="match"
      :loading="isLoading"
    />
  </v-container>
</template>

<script lang="ts" setup>
import { useMutation } from '@pinia/colada'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { defineMessage, useIntl } from 'vue-intl'
import { komgaMatchComicRackList } from '@/generated/openapi'

const intl = useIntl()
const { convertErrorCodes } = useErrorCodeFormatter()

const fileToUpload = ref<File>()
const errorMessage = ref('')

function handleReject() {
  errorMessage.value = intl.formatMessage(unsupportedFileTypeMessage)
}

const {
  data: match,
  mutate: matchCbl,
  isLoading,
} = useMutation({
  mutation: (file: File) =>
    komgaMatchComicRackList({
      body: {
        file: file,
      },
      bodySerializer() {
        const fd = new FormData()
        fd.append('file', file)
        return fd
      },
    }).catch((error) => {
      errorMessage.value =
        convertErrorCodes(error?.cause?.message) ?? intl.formatMessage(commonMessages.networkError)
    }),
})

watch(fileToUpload, (file) => {
  if (file) {
    errorMessage.value = ''
    matchCbl(file)
  }
})

const unsupportedFileTypeMessage = defineMessage({
  description: 'Import readlist view: error message when trying to upload an unsupported file type',
  defaultMessage: 'File type not supported',
  id: 'CxuwFR',
})
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
