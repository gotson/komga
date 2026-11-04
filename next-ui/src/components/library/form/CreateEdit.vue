<template>
  <v-tabs
    v-model="currentTab"
    :items="tabs"
  >
    <template #tab="{ item }">
      <v-tab
        :value="item.value"
        :text="item.text"
      >
        <template #append>
          <v-badge
            :model-value="(tabErrors[item.value] ?? 0) > 0"
            :content="tabErrors[item.value]"
            color="error"
            inline
          />
        </template>
      </v-tab>
    </template>

    <template #[`item.1`]>
      <LibraryFormStepGeneral
        v-model="model"
        @update:error-count="(errorCount) => (tabErrors[1] = errorCount)"
      />
    </template>

    <template #[`item.2`]>
      <LibraryFormStepScanner v-model="model" />
    </template>

    <template #[`item.3`]>
      <LibraryFormStepOptions v-model="model" />
    </template>
    <template #[`item.4`]>
      <LibraryFormStepMetadata v-model="model" />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { LibraryCreationDto } from '@/generated/openapi'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const currentTab = ref(1)
const tabs = [
  {
    value: 1,
    text: intl.formatMessage({
      description: 'Form add/edit library: General',
      defaultMessage: 'General',
      id: 'h6C8/l',
    }),
  },
  {
    value: 2,
    text: intl.formatMessage({
      description: 'Form add/edit library: Scanner',
      defaultMessage: 'Scanner',
      id: 'yaa8so',
    }),
  },
  {
    value: 3,
    text: intl.formatMessage({
      description: 'Form add/edit library: Options',
      defaultMessage: 'Options',
      id: 'uGC9fD',
    }),
  },
  {
    value: 4,
    text: intl.formatMessage({
      description: 'Form add/edit library: Metadata',
      defaultMessage: 'Metadata',
      id: '0iT7Vf',
    }),
  },
]

const tabErrors = ref<Record<number, number>>({})

const model = defineModel<LibraryCreationDto>({ required: true })
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

watch(submitFailed, (attempted) => {
  if (attempted) {
    currentTab.value = 1
    submitFailed.value = false
  }
})
</script>
