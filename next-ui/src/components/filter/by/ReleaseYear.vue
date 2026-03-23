<template>
  <FilterSelectRange
    v-model="model"
    :label-select="
      $formatMessage({
        description: 'Filter by release year: select placeholder',
        defaultMessage: 'Year',
        id: 'tn7uib',
      })
    "
    :label-range="
      $formatMessage({
        description: 'Filter by release year: select placeholder',
        defaultMessage: 'Year rating',
        id: 'S39lnB',
      })
    "
    :select-items="selectItems"
    :min="min"
    :max="max"
    :disabled="disabled"
    :range-mapper="(number) => number.toString()"
  />
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

const model = defineModel<ReleaseYears>({ required: true })

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
</script>

<script lang="ts"></script>

<style scoped></style>
