<template>
  <v-container fluid>
    <v-row>
      <v-col
        cols="12"
        sm="6"
      >
        <BulkTextField
          v-model="model.publisher"
          :initial-counts="initialCounts.publisher"
          :total-count="entities.length"
          :label="$formatMessage(commonMessages.seriesFormGeneralPublisher)"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.publisherLock"
              :initial-counts="initialCountsLock.publisherLock"
              :total-count="entities.length"
            />
          </template>
        </BulkTextField>
      </v-col>

      <v-col
        cols="12"
        sm="6"
      >
        <BulkSelect
          v-model="model.status"
          :initial-counts="initialCounts.status"
          :total-count="entities.length"
          :items="seriesStatusOptions"
          :allow-unset="false"
          :label="$formatMessage(commonMessages.seriesFormGeneralStatus)"
          :disabled="onlyOneShots"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.statusLock"
              :initial-counts="initialCountsLock.statusLock"
              :total-count="entities.length"
              :disabled="onlyOneShots"
            />
          </template>
        </BulkSelect>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        cols="12"
        sm="4"
      >
        <BulkSelect
          v-model="model.readingDirection"
          :initial-counts="initialCounts.readingDirection"
          :total-count="entities.length"
          :items="readingDirectionOptions"
          allow-unset
          :label="$formatMessage(commonMessages.seriesFormGeneralReadingDirection)"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.readingDirectionLock"
              :initial-counts="initialCountsLock.readingDirectionLock"
              :total-count="entities.length"
            />
          </template>
        </BulkSelect>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <BulkTextField
          ref="fieldLanguageRef"
          v-model="model.language"
          :initial-counts="initialCounts.language"
          :total-count="entities.length"
          :rules="
            [['bcp47', $formatMessage(commonMessages.bcp47Error)]] satisfies CustomRuleTuple[]
          "
          :label="$formatMessage(commonMessages.seriesFormGeneralLanguage)"
          :hint="$formatMessage(commonMessages.seriesFormGeneralLanguageHint)"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.languageLock"
              :initial-counts="initialCountsLock.languageLock"
              :total-count="entities.length"
            />
          </template>
        </BulkTextField>
      </v-col>

      <v-col
        cols="12"
        sm="4"
      >
        <BulkNumberInput
          v-model="model.ageRating"
          :min="0"
          :initial-counts="initialCounts.ageRating"
          :total-count="entities.length"
          :label="$formatMessage(commonMessages.seriesFormGeneralAgeRating)"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.ageRatingLock"
              :initial-counts="initialCountsLock.ageRatingLock"
              :total-count="entities.length"
            />
          </template>
        </BulkNumberInput>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { readingDirectionMessages, ReadingDirectionValues } from '@/types/ReadingDirection'
import { useIntl } from 'vue-intl'
import { seriesStatusMessages, SeriesStatusValues } from '@/types/SeriesStatus'
import type { CustomRuleTuple } from '@/plugins/vuetify'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { SeriesMetadataDto } from '@/generated/openapi'
import type { BulkUpdateSeries } from '@/types/entityUpdate'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'
import { seriesMetadataToUpdateDto } from '@/functions/series'
import { useFieldValidity } from '@/composables/form'
import { VTextField } from 'vuetify/components'

const intl = useIntl()

type SeriesBulkUpdateGeneral = Pick<
  BulkUpdateSeries,
  | 'status'
  | 'statusLock'
  | 'language'
  | 'languageLock'
  | 'readingDirection'
  | 'readingDirectionLock'
  | 'publisher'
  | 'publisherLock'
  | 'ageRating'
  | 'ageRatingLock'
>

const model = defineModel<SeriesBulkUpdateGeneral>({ required: true })

const props = defineProps<{
  entities: Pick<
    SeriesMetadataDto,
    | 'status'
    | 'statusLock'
    | 'language'
    | 'languageLock'
    | 'readingDirection'
    | 'readingDirectionLock'
    | 'publisher'
    | 'publisherLock'
    | 'ageRating'
    | 'ageRatingLock'
  >[]
  onlyOneShots: boolean
}>()

const fields = {
  language: useTemplateRef<InstanceType<typeof VTextField>>('fieldLanguageRef'),
}
const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()
useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))

const initialCounts = getInitialCountsForKeys(
  props.entities.map((it) => seriesMetadataToUpdateDto(it)),
  ['status', 'language', 'readingDirection', 'publisher', 'ageRating'],
)

const initialCountsLock = getInitialCountsForKeys(props.entities, [
  'statusLock',
  'languageLock',
  'readingDirectionLock',
  'publisherLock',
  'ageRatingLock',
])

const readingDirectionOptions = ReadingDirectionValues.map((x) => ({
  title: intl.formatMessage(readingDirectionMessages[x]),
  value: x.toString(),
}))

const seriesStatusOptions = SeriesStatusValues.map((x) => ({
  title: intl.formatMessage(seriesStatusMessages[x]),
  value: x.toString(),
}))
</script>
