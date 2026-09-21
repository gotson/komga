<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <v-text-field
          ref="fieldTitleRef"
          v-model="model.title"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form edit series: General - series title',
              defaultMessage: 'Title',
              id: 'No9Upl',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.titleLock" />
          </template>
        </v-text-field>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-text-field
          ref="fieldTitleSortRef"
          v-model="model.titleSort"
          :rules="[rules.required()]"
          :label="$formatMessage(commonMessages.seriesFormGeneralTitleSort)"
        >
          <template #prepend>
            <LockIcon v-model="model.titleSortLock" />
          </template>
        </v-text-field>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-textarea
          ref="fieldSummaryRef"
          v-model="model.summary"
          :label="
            $formatMessage({
              description: 'Form edit series: General - series summary',
              defaultMessage: 'Summary',
              id: 'LKoz42',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.summaryLock" />
          </template>
        </v-textarea>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        cols="12"
        sm="4"
      >
        <v-text-field
          ref="fieldPublisherRef"
          v-model="model.publisher"
          clearable
          :label="$formatMessage(commonMessages.seriesFormGeneralPublisher)"
        >
          <template #prepend>
            <LockIcon v-model="model.publisherLock" />
          </template>
        </v-text-field>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <v-select
          ref="fieldStatusRef"
          v-model="model.status"
          :items="seriesStatusOptions"
          :label="
            $formatMessage({
              description: 'Form edit series: General - series status',
              defaultMessage: 'Publishing status',
              id: '4CIw7i',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.statusLock" />
          </template>
        </v-select>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <v-number-input
          ref="fieldTotalBookCountRef"
          v-model="model.totalBookCount"
          clearable
          :min="1"
          :label="
            $formatMessage({
              description: 'Form edit series: General - series total book count',
              defaultMessage: 'Total book count',
              id: 'Cv11G8',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.totalBookCountLock" />
          </template>
        </v-number-input>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        cols="12"
        sm="4"
      >
        <v-select
          v-model="model.readingDirection"
          :items="readingDirectionOptions"
          clearable
          :label="$formatMessage(commonMessages.seriesFormGeneralReadingDirection)"
        >
          <template #prepend>
            <LockIcon v-model="model.readingDirectionLock" />
          </template>
        </v-select>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <v-text-field
          ref="fieldLanguageRef"
          v-model="model.language"
          clearable
          :rules="
            [['bcp47', $formatMessage(commonMessages.bcp47Error)]] satisfies CustomRuleTuple[]
          "
          :label="$formatMessage(commonMessages.seriesFormGeneralLanguage)"
          :hint="$formatMessage(commonMessages.seriesFormGeneralLanguageHint)"
        >
          <template #prepend>
            <LockIcon v-model="model.languageLock" />
          </template>
        </v-text-field>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <v-number-input
          ref="fieldAgeRatingRef"
          v-model="model.ageRating"
          clearable
          :min="0"
          :label="$formatMessage(commonMessages.seriesFormGeneralAgeRating)"
        >
          <template #prepend>
            <LockIcon v-model="model.ageRatingLock" />
          </template>
        </v-number-input>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { useRules } from 'vuetify'
import { VTextField } from 'vuetify/components'
import { useFieldValidity, useLockWatcher } from '@/composables/form'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'
import { readingDirectionMessages, ReadingDirectionValues } from '@/types/ReadingDirection'
import { useIntl } from 'vue-intl'
import { seriesStatusMessages, SeriesStatusValues } from '@/types/SeriesStatus'
import type { CustomRuleTuple } from '@/plugins/vuetify'
import { watchImmediate } from '@vueuse/core'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()
const rules = useRules()

const vSeriesUpdateGeneral = v.pick(vSeriesMetadataDto, [
  'title',
  'titleLock',
  'titleSort',
  'titleSortLock',
  'summary',
  'summaryLock',
  'status',
  'statusLock',
  'language',
  'languageLock',
  'readingDirection',
  'readingDirectionLock',
  'publisher',
  'publisherLock',
  'ageRating',
  'ageRatingLock',
  'totalBookCount',
  'totalBookCountLock',
])
type SeriesUpdateGeneral = v.InferOutput<typeof vSeriesUpdateGeneral>

const model = defineModel<SeriesUpdateGeneral>({ required: true })
watchImmediate(model, (newModel, oldModel) => {
  if (
    newModel.readingDirection === '' &&
    newModel.readingDirection !== oldModel?.readingDirection
  ) {
    // @ts-expect-error readingDirection from the API can be an empty string, but VSelect expects null for empty. The Series Metadata patch API also expects null.
    model.value.readingDirection = null
  }
})
useLockWatcher(model, vSeriesUpdateGeneral)

const fields = {
  title: useTemplateRef<InstanceType<typeof VTextField>>('fieldTitleRef'),
  titleSort: useTemplateRef<InstanceType<typeof VTextField>>('fieldTitleSortRef'),
  summary: useTemplateRef<InstanceType<typeof VTextField>>('fieldSummaryRef'),
  status: useTemplateRef<InstanceType<typeof VTextField>>('fieldStatusRef'),
  language: useTemplateRef<InstanceType<typeof VTextField>>('fieldLanguageRef'),
  readingDirection: useTemplateRef<InstanceType<typeof VTextField>>('fieldReadingDirectionRef'),
  publisher: useTemplateRef<InstanceType<typeof VTextField>>('fieldPublisherRef'),
  ageRating: useTemplateRef<InstanceType<typeof VTextField>>('fieldAgeRatingRef'),
  totalBookCount: useTemplateRef<InstanceType<typeof VTextField>>('fieldTotalBookCountRef'),
}
const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()
useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))

const readingDirectionOptions = ReadingDirectionValues.map((x) => ({
  title: intl.formatMessage(readingDirectionMessages[x]),
  value: x,
}))

const seriesStatusOptions = SeriesStatusValues.map((x) => ({
  title: intl.formatMessage(seriesStatusMessages[x]),
  value: x,
}))
</script>
