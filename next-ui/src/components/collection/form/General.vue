<template>
  <v-container>
    <v-row>
      <v-col>
        <v-text-field
          ref="fieldNameRef"
          v-model="model.name"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form edit collection: General - collection name',
              defaultMessage: 'Collection name',
              id: 'cfWux+',
            })
          "
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-radio-group
          v-model="model.ordered"
          :label="
            $formatMessage({
              description: 'Form edit collection: General - series ordering',
              defaultMessage: 'Series ordering',
              id: '2uUiqM',
            })
          "
        >
          <v-radio
            :label="
              $formatMessage({
                description: 'Form edit collection: General - series ordering: manually',
                defaultMessage: 'Manually',
                id: 'H1+81t',
              })
            "
            :value="false"
          />
          <v-radio
            :label="
              $formatMessage({
                description: 'Form edit collection: General - series ordering: by name',
                defaultMessage: 'By name',
                id: 'dP6SpT',
              })
            "
            :value="true"
          />
        </v-radio-group>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRules } from 'vuetify'
import type { CollectionUpdateDto } from '@/generated/openapi'
import { VTextField } from 'vuetify/components'
import { useFieldValidity } from '@/composables/form'

const rules = useRules()

const fields = {
  name: useTemplateRef<InstanceType<typeof VTextField>>('fieldNameRef'),
}

type CollectionUpdateGeneral = Pick<CollectionUpdateDto, 'name' | 'ordered'>

const model = defineModel<CollectionUpdateGeneral>({ required: true })

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))
</script>
