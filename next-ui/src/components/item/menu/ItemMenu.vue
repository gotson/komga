<template>
  <div>
    <v-menu
      v-model="rootShown"
      :activator="activator"
      :close-on-content-click="false"
    >
      <v-list density="compact">
        <v-list-item
          v-for="(action, i) in actions"
          :key="i"
          v-bind="action"
          @click="handleItemClick(action)"
        />

        <v-list-item
          v-if="manageActions.length > 0"
          :title="
            $formatMessage({
              description: 'Item menu: manage sub-menu',
              defaultMessage: 'Manage',
              id: 'iq1PzT',
            })
          "
          :append-icon="isRtl ? 'i-mdi:menu-left' : 'i-mdi:menu-right'"
        >
          <v-menu
            v-model="subShown"
            activator="parent"
            open-on-click
            open-on-hover
            location="end"
            submenu
            :close-on-content-click="false"
          >
            <v-list density="compact">
              <v-list-item
                v-for="(action, i) in manageActions"
                :key="i"
                v-bind="action"
                @click="handleItemClick(action)"
              />
            </v-list>
          </v-menu>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script setup lang="ts">
import type { Action } from '@/types/action/action'
import { useRtl } from 'vuetify/framework'

const { isRtl } = useRtl()

const { actions = [], manageActions = [] } = defineProps<{
  activator: string | Element
  actions?: Action<unknown>[]
  manageActions?: Action<unknown>[]
}>()

const rootShown = ref(false)
const subShown = ref(false)

function handleItemClick(action: Action<unknown>): void {
  if (action?.onClick) {
    action.onClick()
  }

  subShown.value = false
  rootShown.value = false
}
</script>

<script lang="ts"></script>

<style scoped></style>
