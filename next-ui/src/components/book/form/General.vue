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
              description: 'Form edit book: General - book title',
              defaultMessage: 'Title',
              id: 'QBQ5a/',
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
          ref="fieldNumberRef"
          v-model="model.number"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form edit book: General - book number',
              defaultMessage: 'Number',
              id: '7Ndu8p',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.numberLock" />
          </template>
        </v-text-field>
      </v-col>

      <v-col>
        <v-text-field
          ref="fieldNumberSortRef"
          v-model="model.numberSort"
          type="number"
          step="0.1"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form edit book: General - book sort number',
              defaultMessage: 'Sort number',
              id: 'q4YedO',
            })
          "
          :hint="
            $formatMessage({
              description: 'Form edit book: General - book sort number, hint',
              defaultMessage: 'Decimal numbers can be used',
              id: 'Wi+k3O',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.numberSortLock" />
          </template>
        </v-text-field>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-textarea
          v-model="model.summary"
          :label="
            $formatMessage({
              description: 'Form edit book: General - book summary',
              defaultMessage: 'Summary',
              id: '7RfNiY',
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
        sm="6"
      >
        <v-date-input
          ref="fieldReleaseDateRef"
          v-model="model.releaseDate"
          clearable
          :label="
            $formatMessage({
              description: 'Form edit book: General - book release date',
              defaultMessage: 'Release date',
              id: 'x1MEvu',
            })
          "
        >
          <template #prepend>
            <LockIcon v-model="model.releaseDateLock" />
          </template>
        </v-date-input>
      </v-col>

      <v-col
        cols="12"
        sm="6"
      >
        <v-text-field
          ref="fieldIsbnRef"
          v-model="model.isbn"
          clearable
          :label="
            $formatMessage({
              description: 'Form edit book: General - book ISBN',
              defaultMessage: 'ISBN',
              id: 's/A/wz',
            })
          "
          placeholder="978-2-20-504375-4"
          :rules="
            [
              [
                'isbn13',
                $formatMessage({
                  description: 'Form edit book: General - Error message if ISBN is invalid',
                  defaultMessage: 'Must be a valid ISBN 13',
                  id: 'TtH2LJ',
                }),
              ],
            ] satisfies CustomRuleTuple[]
          "
        >
          <template #prepend>
            <LockIcon v-model="model.isbnLock" />
          </template>
        </v-text-field>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { useRules } from 'vuetify'
import { VTextField } from 'vuetify/components'
import { useFieldValidity, useLockWatcher } from '@/composables/form'
import type { CustomRuleTuple } from '@/plugins/vuetify'
import { vBookMetadataDto } from '@/generated/openapi/valibot.gen'

const rules = useRules()

const vBookUpdateGeneral = v.pick(vBookMetadataDto, [
  'title',
  'titleLock',
  'number',
  'numberLock',
  'numberSort',
  'numberSortLock',
  'summary',
  'summaryLock',
  'releaseDate',
  'releaseDateLock',
  'isbn',
  'isbnLock',
])
type BookUpdateGeneral = v.InferOutput<typeof vBookUpdateGeneral>

const model = defineModel<BookUpdateGeneral>({ required: true })
useLockWatcher(model, vBookUpdateGeneral)

const fields = {
  title: useTemplateRef<InstanceType<typeof VTextField>>('fieldTitleRef'),
  number: useTemplateRef<InstanceType<typeof VTextField>>('fieldNumberRef'),
  numberSort: useTemplateRef<InstanceType<typeof VTextField>>('fieldNumberSortRef'),
  releaseDate: useTemplateRef<InstanceType<typeof VTextField>>('fieldReleaseDateRef'),
  isbn: useTemplateRef<InstanceType<typeof VTextField>>('fieldIsbnRef'),
}
const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()
useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))
</script>
