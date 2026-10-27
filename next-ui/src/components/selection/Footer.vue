<template>
  <v-chip
    class="position-absolute top-0 left-0 mt-2 ms-2"
    style="z-index: 2000"
    variant="elevated"
    color="primary"
    rounded
    size="x-large"
  >
    <v-icon
      icon="i-mdi:close"
      class="me-3"
      @click.stop="selectionStore.clear()"
    />
    <v-icon
      v-for="action in selectionStore.contextualActions"
      :key="action.text"
      :icon="action.icon"
      class="me-3"
      @click.stop="action.callback()"
    />
    <span>{{ selectionStore.count }}</span>
  </v-chip>

  <v-footer
    app
    elevation="5"
    class="pa-1"
    color="primary"
  >
    <v-slide-group>
      <v-slide-group-item
        v-for="action in actions"
        :key="action.title"
      >
        <v-menu
          :disabled="!action.menuItems"
          location="top center"
          origin="bottom center"
        >
          <template #activator="{ props: menuProps }">
            <v-btn
              v-if="action.min ? selectionStore.count >= action.min : true"
              :prepend-icon="action.icon"
              stacked
              variant="text"
              class="h-auto pt-2 pb-1"
              color=""
              rounded="0"
              v-bind="{ ...action, ...(!action.menuItems ? {} : menuProps) }"
              :icon="undefined"
            >
              <span
                class="text-caption mt-1 text-wrap text-center d-flex align-start justify-center"
                style="width: 72px; min-height: 2.8em; line-height: 1.2"
              >
                {{ action.title }}
              </span>
            </v-btn>
          </template>

          <v-list density="comfortable">
            <v-list-item
              v-for="(entry, index) in action.menuItems"
              :key="index"
              v-bind="entry"
              :disabled="entry.min ? selectionStore.count < entry.min : false"
            />
          </v-list>
        </v-menu>
      </v-slide-group-item>
    </v-slide-group>
  </v-footer>
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'
import { useEntitiesActions } from '@/composables/useEntitiesActions'
import { storeToRefs } from 'pinia'

const selectionStore = useSelectionStore()
const { selectionAsArray } = storeToRefs(selectionStore)

function afterClick() {
  selectionStore.clear()
}

const { actions } = useEntitiesActions(selectionAsArray, afterClick)
</script>
