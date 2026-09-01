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
import { useRules } from 'vuetify/labs/rules'
import type { CollectionUpdateDto } from '@/generated/openapi'
import { VTextField } from 'vuetify/components'

const rules = useRules()

const fieldNameRef = ref<InstanceType<typeof VTextField> | null>(null)

type CollectionUpdateGeneral = Pick<CollectionUpdateDto, 'name' | 'ordered'>

const model = defineModel<CollectionUpdateGeneral>({ required: true })

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

watch([() => fieldNameRef.value?.isValid], ([nameValid]) => {
  const fields = [nameValid]
  emit('update:errorCount', fields.filter((it) => it === false).length)
})
</script>
