<template>
  <div>
    <v-tabs
      v-model="tab"
      :items="tabs"
    />

    <v-tabs-window v-model="tab">
      <v-tabs-window-item value="tab-edit">
        <v-sheet class="pa-4">
          <ReadlistFormGeneral v-model="model.entity" />
        </v-sheet>
      </v-tabs-window-item>

      <v-tabs-window-item value="tab-poster">
        <v-sheet
          class="pa-4"
          style="max-height: 80vh; overflow-y: auto"
        >
          <PosterUpload
            :entity-posters="entityPosters ?? []"
            @upload-queue-changed="(it) => (model.uploadQueue = it)"
            @delete-queue-changed="(it) => (model.deleteQueue = it)"
            @poster-selected="(it) => (model.selected = it)"
          />
        </v-sheet>
      </v-tabs-window-item>
    </v-tabs-window>
  </div>
</template>

<script setup lang="ts">
import type { ReadListDto } from '@/generated/openapi'
import type { EntityUpdate } from '@/functions/poster'
import { useQuery } from '@pinia/colada'
import { readListPostersQuery } from '@/colada/readlists'

const model = defineModel<EntityUpdate<ReadListDto>>({ required: true })

const tabs = [
  {
    text: 'Edit',
    value: 'tab-edit',
  },
  {
    text: 'Poster',
    value: 'tab-poster',
  },
]
const tab = shallowRef('tab-edit')

const { data: entityPosters } = useQuery(() =>
  readListPostersQuery({
    readListId: model.value.entity.id,
  }),
)
</script>
