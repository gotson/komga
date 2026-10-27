<template>
  <v-menu
    v-model="menuOpen"
    :close-on-content-click="true"
    location="bottom end"
    :target="`.${fieldId} .v-field`"
    :disabled="!hasPresetValues"
  >
    <template #activator="{ props: menuProps }">
      <v-number-input
        v-bind="{ ...$attrs, ...(hasPresetValues ? menuProps : {}) }"
        v-model="inputModel"
        :class="fieldId"
        :placeholder="placeholder"
        :persistent-hint="unset"
        :hint="unset ? $formatMessage(commonMessages.bulkEditUnset) : undefined"
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

        <template #prepend>
          <slot name="prepend" />
        </template>
      </v-number-input>
    </template>

    <!-- Dropdown List of Existing Item Values -->
    <v-list>
      <v-list-subheader>{{
        $formatMessage(commonMessages.bulkEditExistingValues)
      }}</v-list-subheader>
      <v-list-item
        v-for="(val, index) in uniquePresetValues"
        :key="index"
        @click="selectPresetValue(val.value)"
      >
        <v-list-item-title>{{ val.text }}</v-list-item-title>
        <template #append>
          <v-chip
            size="x-small"
            variant="flat"
            rounded
          >
            {{ val.count }}
          </v-chip>
        </template>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { BulkInputProps, BulkItemSingleField } from '@/types/bulk'
import { initializeBulkEdit } from '@/functions/bulkEdit'
import { deepEqual } from 'fast-equals'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()

defineOptions({
  inheritAttrs: false,
})

const model = defineModel<BulkItemSingleField<number | null>>()
const props = defineProps<BulkInputProps<number | null>>()

const fieldId = useId()
const menuOpen = ref<boolean>(false)

const { initialValue, isMixed } = initializeBulkEdit(props, null)
if (!model.value) {
  model.value = initialValue
}
const initialValueNull = initialValue.state !== 'mixed' && typeof initialValue.value !== 'number'
const placeholder = computed(() => {
  return unset.value || initialValueNull
    ? intl.formatMessage(commonMessages.bulkEditNoValue)
    : isMixed
      ? intl.formatMessage(commonMessages.bulkEditMultipleValues)
      : undefined
})

const uniquePresetValues = computed(() =>
  props.initialCounts
    .map((it) => ({
      text: it.value ?? intl.formatMessage(commonMessages.bulkEditNoValue),
      value: it.value,
      count: it.count,
    }))
    .toSorted((a, b) => (a.value ?? Infinity) - (b.value ?? Infinity)),
)
const hasPresetValues = computed(() => uniquePresetValues.value.length > 0)

const inputModel = ref<number | null>(initialValue.state === 'mixed' ? null : initialValue.value)
const inputModelIsUpdating = ref(false)
const unset = ref(false)

const isDirty = computed(() => !deepEqual(model.value, initialValue))
const canUnset = computed(() => !unset.value && !initialValueNull)

watch(inputModel, (newVal) => {
  if (inputModelIsUpdating.value) {
    void nextTick(() => (inputModelIsUpdating.value = false))
  } else if (typeof newVal === 'number') {
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

function selectPresetValue(preset: number | null) {
  if (typeof preset === 'number') inputModel.value = preset
  else if (!initialValueNull) handleUnset()
  else handleReset()
}
</script>
