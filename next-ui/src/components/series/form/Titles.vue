<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <v-btn
        prepend-icon="i-mdi:plus"
        rounded="lg"
        :text="
          $formatMessage({
            description: 'Form edit: Alternate titles: add button',
            defaultMessage: 'Add alternate title',
            id: 'D+ZsSd',
          })
        "
        border
        class="me-2"
        @click="addTitle()"
      />

      <LockIcon v-model="model.alternateTitlesLock" />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="(title, i) in model.alternateTitles"
        :key="i"
      >
        <v-col cols="3">
          <v-text-field
            ref="fieldLabelRef"
            v-model="title.label"
            :rules="[rules.required()]"
            :label="
              $formatMessage({
                description: 'Form edit: Alternate titles - Label field label',
                defaultMessage: 'Label',
                id: '4IKM3R',
              })
            "
          />
        </v-col>

        <v-col cols="9">
          <v-text-field
            ref="fieldTitleRef"
            v-model="title.title"
            :rules="[rules.required()]"
            :label="
              $formatMessage({
                description: 'Form edit: Alternate titles - alternate title field label',
                defaultMessage: 'Alternate title',
                id: '+PNCoe',
              })
            "
            append-icon="i-mdi:trash"
            @click:append="removeAtIndex(i)"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useFieldValidity, useLockWatcher } from '@/composables/form'
import { useRules } from 'vuetify'
import { VTextField } from 'vuetify/components'
import * as v from 'valibot'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'

const rules = useRules()

const vSeriesUpdateTitles = v.pick(vSeriesMetadataDto, ['alternateTitles', 'alternateTitlesLock'])
type SeriesUpdateTitles = v.InferOutput<typeof vSeriesUpdateTitles>

const model = defineModel<SeriesUpdateTitles>({ required: true })
useLockWatcher(model, vSeriesUpdateTitles)

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

const fields = {
  label: useTemplateRef<InstanceType<typeof VTextField>[]>('fieldLabelRef'),
  title: useTemplateRef<InstanceType<typeof VTextField>[]>('fieldTitleRef'),
}
useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))

function addTitle() {
  model.value.alternateTitles.push({ label: '', title: '' })
}

function removeAtIndex(index: number) {
  model.value.alternateTitles.splice(index, 1)
}
</script>
