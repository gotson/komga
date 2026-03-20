<template>
  <v-select
    v-model="model.is"
    label="Year"
    clearable
    hide-details
    :items="selectItems"
    @click:clear="model.is = undefined"
  />
  <div class="px-2 pt-2">
    <div class="d-flex justify-space-between align-center mb-2">
      <span
        class="text-body-large"
        style="min-height: 26px"
        >Year range</span
      >
      <v-chip
        v-if="!!model.min && !!model.max"
        :disabled="isSingle"
        closable
        color="primary"
        rounded
        size="small"
        variant="elevated"
        @click:close="clearRange()"
        >{{ model.min }} - {{ model.max }}
      </v-chip>
    </div>

    <v-range-slider
      v-model="modelRange"
      strict
      hide-details
      :disabled="disabled || isSingle"
      :step="1"
      :min="min"
      :max="max"
      thumb-label="hover"
    />
  </div>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { filterKeys, filterMessages, SchemaSeriesReleaseYears } from '@/types/filter'
import { useQuery } from '@pinia/colada'
import { releaseYearsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { useIntl } from 'vue-intl'

const intl = useIntl()

type ReleaseYears = v.InferOutput<typeof SchemaSeriesReleaseYears>

const model = defineModel<ReleaseYears>({ default: [] })

const filterContext = inject(filterKeys.context, {})

const apiQuery = {
  ...filterContext,
}

const { data: items } = useQuery(() => ({
  ...releaseYearsQuery({
    pageRequest: PageRequest.Unpaged(),
    ...apiQuery,
  }),
}))
const disabled = computed(() => (items.value?.totalElements || 0) === 0)
const isSingle = computed(() => model.value.is !== undefined)

const selectItems = computed(() => [
  {
    title: intl.formatMessage(filterMessages.any!),
    value: 'any',
  },
  {
    title: intl.formatMessage(filterMessages.none!),
    value: 'none',
  },
  ...(items.value?.content?.map((it) => ({ title: it, value: it })) || []),
])

const min = computed(() => items.value?.content?.map((it) => Number(it))?.at(-1))
const max = computed(() => items.value?.content?.map((it) => Number(it))?.at(0))

const modelRange = computed({
  get: () => [model.value.min || 0, model.value.max || 10000],
  set: (newValue) => {
    model.value.min = newValue?.[0]?.toString()
    model.value.max = newValue?.[1]?.toString()
  },
})

function clearRange() {
  model.value.min = undefined
  model.value.max = undefined
}
</script>

<script lang="ts"></script>

<style scoped></style>
