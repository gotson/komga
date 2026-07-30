<template>
  <v-list>
    <VueDraggable
      v-model="localSections"
      :animation="150"
      ghost-class="ghost"
      target=".sort-target"
      @start="drag = true"
      @end="nextTick(() => (drag = false))"
    >
      <div class="sort-target">
        <v-fade-transition
          group
          :disabled="drag"
        >
          <v-list-item
            v-for="section in localSections"
            :key="section.section"
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
        </v-fade-transition>
      </div>
    </VueDraggable>

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
import { VueDraggable } from 'vue-draggable-plus'
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

const drag = ref(false)
</script>

<style lang="scss">
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
