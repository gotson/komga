<template>
  <div>
    <v-menu :activator="activator">
      <v-list density="compact">
        <v-list-item
          v-for="(action, i) in actions"
          :key="i"
          v-bind="action"
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
          append-icon="i-mdi:menu-right"
        >
          <v-menu
            activator="parent"
            open-on-click
            open-on-hover
            location="end"
            submenu
          >
            <v-list density="compact">
              <v-list-item
                v-for="(action, i) in manageActions"
                :key="i"
                v-bind="action"
              />
            </v-list>
          </v-menu>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script setup lang="ts">
import type { Action } from '@/types/action'

const { actions = [], manageActions = [] } = defineProps<{
  activator: string | Element
  actions?: Action<unknown>[]
  manageActions?: Action<unknown>[]
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
