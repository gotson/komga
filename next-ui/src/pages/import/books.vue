<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <ImportBooksDirectorySelection
      v-model:error-messages="errorMessage"
      :loading="isLoading"
      @scan="(directory) => doScan(directory)"
    />
    <ImportBooksTransientBooksTable
      v-if="transientBooks"
      :loading="isLoading"
      :books="transientBooks"
    />
  </v-container>
</template>

<script lang="ts" setup>
import { transientBooksScan } from '@/colada/transient-books'
import { useQuery } from '@pinia/colada'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useIntl } from 'vue-intl'

const intl = useIntl()
const { convertErrorCodes } = useErrorCodeFormatter()

const scanDirectory = ref<string>('')
const errorMessage = ref('')

const {
  data: transientBooks,
  isLoading,
  refetch,
} = useQuery(() =>
  transientBooksScan({
    path: scanDirectory.value,
  }),
)

function doScan(directory: string) {
  errorMessage.value = ''
  scanDirectory.value = directory
  refetch(true).catch((error) => {
    errorMessage.value =
      convertErrorCodes(error?.cause?.message) ?? intl.formatMessage(commonMessages.networkError)
  })
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
