<template>
  <FilterSelectRange
    v-model="model"
    :label-select="
      $formatMessage({
        description: 'Filter by age rating: select placeholder',
        defaultMessage: 'Age rating',
        id: 'KBLD9U',
      })
    "
    :label-range="
      $formatMessage({
        description: 'Filter by age rating: range label',
        defaultMessage: 'Age range',
        id: '5zPpyT',
      })
    "
    :select-items="selectItems"
    :min="min"
    :max="max"
    :disabled="disabled"
  />
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { filterKeys, filterMessages, SchemaSeriesAgeRatings } from '@/types/filter'
import { useQuery } from '@pinia/colada'
import { ageRatingsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { useIntl } from 'vue-intl'

const intl = useIntl()

type AgeRatings = v.InferOutput<typeof SchemaSeriesAgeRatings>

const model = defineModel<AgeRatings>({ required: true })

const filterContext = inject(filterKeys.context, {})

const apiQuery = {
  ...filterContext,
}

const { data: items } = useQuery(() => ({
  ...ageRatingsQuery({
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

const min = computed(() => items.value?.content?.map((it) => Number(it))?.at(0))
const max = computed(() => items.value?.content?.map((it) => Number(it))?.at(-1))
</script>

<script lang="ts"></script>

<style scoped></style>
