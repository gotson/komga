<template>
  <v-container class="px-0">
    <v-row>
      <v-col>
        <v-text-field
          v-model="model.name"
          :rules="['required']"
          :label="
            $formatMessage({
              description: 'Form add/edit library: General - library name',
              defaultMessage: 'Library name',
              id: 's1nzhU',
            })
          "
          hide-details="auto"
        />
      </v-col>
    </v-row>

    <v-row align="center">
      <v-col>
        <v-text-field
          v-model="model.root"
          :rules="['required']"
          :label="
            $formatMessage({
              description: 'Form add/edit library: General - root directory',
              defaultMessage: 'Root directory',
              id: 'afXGQS',
            })
          "
          hide-details="auto"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn
          id="ID01KC5HANV8QMDAW8GW4HFZCY0B"
          :text="
            $formatMessage({
              description: 'Form add/edit library: General - root folder browse button',
              defaultMessage: 'Browse',
              id: 'E1kQun',
            })
          "
        />
      </v-col>
    </v-row>
  </v-container>

  <DialogConfirmEdit
    v-model:record="model.root"
    :title="
      $formatMessage({
        description: 'Form add/edit library: General - root directory selection dialog title',
        defaultMessage: 'Library root directory',
        id: 'CJaS7j',
      })
    "
    max-width="600"
    close-on-save
    scrollable
    :fullscreen="display.xs.value"
    activator="#ID01KC5HANV8QMDAW8GW4HFZCY0B"
    @update:record="(val) => (model.root = val as string)"
  >
    <template #text="{ proxyModel }">
      <RemoteFileList v-model="proxyModel.value as string" />
    </template>
  </DialogConfirmEdit>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import RemoteFileList from '@/components/RemoteFileList.vue'
import { useDisplay } from 'vuetify'

const display = useDisplay()

type LibraryCreationGeneral = Pick<components['schemas']['LibraryCreationDto'], 'name' | 'root'>

const model = defineModel<LibraryCreationGeneral>({ required: true })
</script>
