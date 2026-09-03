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
              description: 'Form edit read list: General - read list name',
              defaultMessage: 'Read list name',
              id: 'UTPZNG',
            })
          "
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-textarea
          v-model="model.summary"
          :label="
            $formatMessage({
              description: 'Form edit read list: General - read list summary',
              defaultMessage: 'Summary',
              id: 'TTO9NT',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-radio-group
          v-model="model.ordered"
          :label="
            $formatMessage({
              description: 'Form edit read list: General - books ordering',
              defaultMessage: 'Books ordering',
              id: 'uD9n5o',
            })
          "
        >
          <v-radio
            :label="
              $formatMessage({
                description: 'Form edit read list: General - books ordering: manually',
                defaultMessage: 'Manually',
                id: 'bRxSgh',
              })
            "
            :value="false"
          />
          <v-radio
            :label="
              $formatMessage({
                description: 'Form edit read list: General - books ordering: by release date',
                defaultMessage: 'By release date',
                id: 'TfdugD',
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
import type { ReadListUpdateDto } from '@/generated/openapi'
import { VTextField } from 'vuetify/components'

const rules = useRules()

const fieldNameRef = ref<InstanceType<typeof VTextField> | null>(null)

type ReadListUpdateGeneral = Pick<ReadListUpdateDto, 'name' | 'ordered' | 'summary'>

const model = defineModel<ReadListUpdateGeneral>({ required: true })

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

watch([() => fieldNameRef.value?.isValid], ([nameValid]) => {
  const fields = [nameValid]
  emit('update:errorCount', fields.filter((it) => it === false).length)
})
</script>
