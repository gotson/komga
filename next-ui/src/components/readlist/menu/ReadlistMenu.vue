<template>
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
            description: 'Readlist menu: manage',
            defaultMessage: 'Manage read list',
            id: 'j8SRo3',
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
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useReadListActions } from '@/composables/readlist/useReadListActions'

const props = defineProps<{
  activator: string | Element
  readList: components['schemas']['ReadListDto']
}>()

const { actions, manageActions } = useReadListActions(() => props.readList)
</script>

<script lang="ts"></script>

<style scoped></style>
