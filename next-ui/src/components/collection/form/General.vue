<template>
  <v-container>
    <v-row>
      <v-col>
        <v-text-field
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
import { useRules } from 'vuetify/labs/rules'
import type { CollectionUpdateDto } from '@/generated/openapi'

const rules = useRules()

type CollectionUpdateGeneral = Pick<CollectionUpdateDto, 'name' | 'ordered'>

const model = defineModel<CollectionUpdateGeneral>({ required: true })
</script>
