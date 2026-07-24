<template>
  <v-list>
    <draggable
      v-model="localSections"
      v-bind="draggableConfig"
    >
      <template #[`item`]="{ element: section }: { element: LocalSection }">
        <v-list-item
          :title="$formatMessage(overviewSectionMessages[section.section])"
          prepend-icon="i-mdi:drag-horizontal"
          class="cursor-grab"
        >
          <template #append>
            <v-switch
              v-model="section.enabled"
              true-icon="i-mdi:check"
              false-icon="i-mdi:close"
              color="primary"
              inset="material"
              hide-details
            />
          </template>
        </v-list-item>
      </template>
    </draggable>

    <v-list-item class="text-center">
      <v-btn
        :disabled="isDefault"
        prepend-icon="i-mdi:restore"
        :text="
          $formatMessage({
            description: 'Reorder sections: restore defaults button',
            defaultMessage: 'Restore defaults',
            id: 'Dn4qvV',
          })
        "
        variant="text"
        color=""
        @click="restoreDefaults()"
      />
    </v-list-item>
  </v-list>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import { type ClientSettingUserOverviewSection } from '@/types/ClientSettingsUser'
import { overviewSectionMessages, OverviewSectionsDefault } from '@/types/OverviewSection'
import { watchDeep } from '@vueuse/core'

const model = defineModel<ClientSettingUserOverviewSection[]>({ required: true })

const missingSections = computed(() =>
  OverviewSectionsDefault.filter(
    (section) => !model.value.some((it) => section.section === it.section),
  ),
)

type LocalSection = { enabled: boolean } & ClientSettingUserOverviewSection

const localSections = ref<LocalSection[]>([
  ...model.value.map((it) => ({ enabled: true, ...it })),
  ...missingSections.value.map((it) => ({ enabled: false, ...it })),
])

watchDeep(localSections, (newVal) => {
  model.value = newVal.filter((it) => it.enabled).map(({ enabled, ...rest }) => rest)
})

const defaultSections = OverviewSectionsDefault.map((it) => ({ enabled: true, ...it }))

function restoreDefaults() {
  localSections.value = structuredClone(defaultSections)
}

const isDefault = computed(
  () => JSON.stringify(defaultSections) === JSON.stringify(localSections.value),
)

const draggableConfig = {
  group: 'libs',
  itemKey: 'id',
  ghostClass: 'ghost',
  chosenClass: 'chosen',
  animation: 150,
}
</script>

<style lang="scss">
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
