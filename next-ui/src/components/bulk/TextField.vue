<template>
  <v-combobox
    v-model="inputModel"
    v-model:menu="menuOpen"
    :placeholder="placeholder"
    persistent-placeholder
    :persistent-hint="unset"
    :hint="unset ? $formatMessage(commonMessages.bulkEditUnset) : undefined"
    :items="uniquePresetValues"
  >
    <template #append-inner>
      <v-btn
        v-if="canUnset"
        icon="$clear"
        color="medium-emphasis"
        variant="text"
        density="compact"
        class="me-1"
        @click.stop="handleUnset()"
      />
      <v-btn
        v-if="isDirty"
        icon="i-mdi:restore"
        color="medium-emphasis"
        variant="text"
        density="compact"
        class="me-1"
        @click.stop="handleReset()"
      />
    </template>

    <template #item="{ props: itemProps, item }">
      <v-list-item v-bind="itemProps">
        <template #append>
          <v-chip
            v-if="getItemCount(item)"
            size="x-small"
            variant="flat"
            rounded
          >
            {{ getItemCount(item) }}
          </v-chip>
        </template>
      </v-list-item>
    </template>

    <template
      v-if="nullCount"
      #append-item
    >
      <v-list-item
        :title="$formatMessage(commonMessages.bulkEditNoValue)"
        @click="handleSelectNull()"
      >
        <template #append>
          <v-chip
            size="x-small"
            variant="flat"
            rounded
          >
            {{ nullCount }}
          </v-chip>
        </template>
      </v-list-item>
    </template>

    <template #prepend>
      <slot name="prepend" />
    </template>
  </v-combobox>
</template>

<script setup lang="ts">
import type { BulkInputProps, BulkItemSingleField } from '@/types/bulk'
import { initializeBulkEdit } from '@/functions/bulkEdit'
import { deepEqual } from 'fast-equals'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()

const model = defineModel<BulkItemSingleField<string | null>>()
const props = defineProps<BulkInputProps<string | null>>()

const menuOpen = ref<boolean>(false)

const { initialValue, isMixed } = initializeBulkEdit(props, null)
if (!model.value) {
  model.value = initialValue
}
const initialValueNull = initialValue.state !== 'mixed' && typeof initialValue.value !== 'string'
const placeholder = computed(() => {
  return unset.value || initialValueNull
    ? intl.formatMessage(commonMessages.bulkEditNoValue)
    : isMixed
      ? intl.formatMessage(commonMessages.bulkEditMultipleValues)
      : undefined
})

const inputModel = ref<string | null>(initialValue.state === 'mixed' ? null : initialValue.value)
const inputModelIsUpdating = ref(false)
const unset = ref(false)

const isDirty = computed(() => !deepEqual(model.value, initialValue))
const canUnset = computed(() => !unset.value && !initialValueNull)

function getItemCount(value: string | null): number | undefined {
  return props.initialCounts.find((it) => (it.value ?? '') === (value ?? ''))?.count
}
const nullCount = computed(() => getItemCount(null))

const uniquePresetValues = computed(() =>
  props.initialCounts
    .map((it) => it.value)
    .filter((val): val is string => Boolean(val && val.trim() !== ''))
    .toSorted((a, b) => a.localeCompare(b)),
)

watch(inputModel, (newVal) => {
  if (inputModelIsUpdating.value) {
    void nextTick(() => (inputModelIsUpdating.value = false))
  } else if (newVal !== '') {
    unset.value = false
    model.value = {
      state: 'all',
      value: newVal,
    }
  } else {
    if (isMixed || initialValueNull) {
      // reset
      unset.value = false
      model.value = initialValue
    } else {
      // unset
      unset.value = true
      model.value = {
        state: 'all',
        value: null,
      }
    }
  }
})

function handleUnset() {
  unset.value = true
  model.value = {
    state: 'all',
    value: null,
  }
  inputModelIsUpdating.value = true
  inputModel.value = null
}

function handleReset() {
  unset.value = false
  model.value = initialValue
  inputModelIsUpdating.value = true
  inputModel.value = initialValue.state === 'mixed' ? null : initialValue.value
}

function handleSelectNull() {
  if (initialValueNull) handleReset()
  else handleUnset()
  menuOpen.value = false
}
</script>
