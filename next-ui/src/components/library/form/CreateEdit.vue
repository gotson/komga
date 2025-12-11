<template>
  <v-stepper-vertical
    :hide-actions="editMode"
    eager
    flat
  >
    <template #default="{ step }">
      <v-stepper-vertical-item
        :title="
          $formatMessage({
            description: 'Form add/edit library: General',
            defaultMessage: 'General',
            id: 'h6C8/l',
          })
        "
        value="1"
        :complete="createMode && step > 1"
        :editable="editMode"
      >
        <LibraryFormStepGeneral v-model="model" />

        <template #next="{ next }">
          <v-btn
            color="primary"
            :disabled="!model.name || !model.root"
            @click="next"
          ></v-btn>
        </template>

        <template #prev></template>
      </v-stepper-vertical-item>

      <v-stepper-vertical-item
        :title="
          $formatMessage({
            description: 'Form add/edit library: Scanner',
            defaultMessage: 'Scanner',
            id: 'yaa8so',
          })
        "
        value="2"
        :complete="createMode && step > 2"
        :editable="editMode"
      >
        <LibraryFormStepScanner v-model="model" />

        <template #next="{ next }">
          <v-btn
            color="primary"
            @click="next"
          ></v-btn>
        </template>

        <template #prev="{ prev }">
          <v-btn
            variant="plain"
            @click="prev"
          ></v-btn>
        </template>
      </v-stepper-vertical-item>

      <v-stepper-vertical-item
        :title="
          $formatMessage({
            description: 'Form add/edit library: Options',
            defaultMessage: 'Options',
            id: 'uGC9fD',
          })
        "
        value="3"
        :complete="createMode && step > 3"
        :editable="editMode"
      >
        <LibraryFormStepOptions v-model="model" />

        <template #next="{ next }">
          <v-btn
            color="primary"
            @click="next"
          ></v-btn>
        </template>

        <template #prev="{ prev }">
          <v-btn
            variant="plain"
            @click="prev"
          ></v-btn>
        </template>
      </v-stepper-vertical-item>

      <v-stepper-vertical-item
        :title="
          $formatMessage({
            description: 'Form add/edit library: Metadata',
            defaultMessage: 'Metadata',
            id: '0iT7Vf',
          })
        "
        value="4"
        :complete="createMode && step > 4"
        :editable="editMode"
      >
        <LibraryFormStepMetadata v-model="model" />

        <template #next=""></template>

        <template #prev="{ prev }">
          <v-btn
            variant="plain"
            @click="prev"
          ></v-btn>
        </template>
      </v-stepper-vertical-item>
    </template>
  </v-stepper-vertical>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'

const { createMode } = defineProps<{
  createMode: boolean
}>()

const editMode = computed(() => !createMode)

const model = defineModel<components['schemas']['LibraryCreationDto']>({ required: true })
</script>
