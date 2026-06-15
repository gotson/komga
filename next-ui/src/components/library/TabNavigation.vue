<template>
  <v-app-bar>
    <template #prepend>
      <LibraryHeader
        v-if="isSingle"
        :library="librarySingle!"
        class="ms-4"
      />

      <v-menu v-if="!isSingle">
        <template #activator="{ props: activatorProps }">
          <v-btn
            class="text-title-large"
            height="100%"
            rounded="0"
            variant="text"
            v-bind="activatorProps"
            append-icon="i-mdi:menu-down"
            :text="selectedLibType?.title"
          />
        </template>

        <v-list>
          <v-list-item
            v-for="route in libTypes"
            :key="route.value"
            :title="route.title"
            :to="route.to"
          />
        </v-list>
      </v-menu>
    </template>

    <template
      v-if="display.smAndUp.value"
      #default
    >
      <v-tabs
        :items="routes"
        center-active
      >
        <template #tab="{ item: route }">
          <v-tab
            :text="route.title"
            :to="route.to"
          />
        </template>
      </v-tabs>
    </template>

    <template #append>
      <!-- Empty slot effectively centers the tabs   -->
      <v-menu v-if="display.xs.value">
        <template #activator="{ props: activatorProps }">
          <v-btn
            class="text-title-large"
            height="100%"
            rounded="0"
            variant="text"
            v-bind="activatorProps"
            append-icon="i-mdi:menu-down"
            :text="selectedRoute?.title"
          />
        </template>

        <v-list>
          <v-list-item
            v-for="route in routes"
            :key="route.title"
            :title="route.title"
            :to="route.to"
          />
        </v-list>
      </v-menu>
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
import type { Route } from '@/types/route'
import type { LibraryId } from '@/types/libraries'
import { useGetLibrariesById } from '@/composables/libraries'
import { useIntl } from 'vue-intl'
import { useLibraries } from '@/colada/libraries'
import { useDisplay } from 'vuetify/framework'

const props = defineProps<{
  routes: Route[]
  libraryId: LibraryId
}>()

const intl = useIntl()
const display = useDisplay()
const currentRoute = useRoute()

const { anyPinned, anyUnpinned } = useLibraries()
const { isSingle, library: librarySingle } = useGetLibrariesById(props.libraryId)

const libTypes = computed(() => [
  ...(anyPinned.value
    ? [
        {
          title: intl.formatMessage({
            description: 'Library tab navigation: library selection: pinned',
            defaultMessage: 'Pinned',
            id: '1qIfds',
          }),
          value: 'pinned',
          to: `/libraries/pinned`,
        },
      ]
    : []),
  {
    title: intl.formatMessage({
      description: 'Library tab navigation: library selection: all',
      defaultMessage: 'All',
      id: '8/BXfN',
    }),
    value: 'all',
    to: `/libraries/all`,
  },
  ...(anyUnpinned.value
    ? [
        {
          title: intl.formatMessage({
            description: 'Library tab navigation: library selection: unpinned',
            defaultMessage: 'Unpinned',
            id: '9oA9gw',
          }),
          value: 'unpinned',
          to: `/libraries/unpinned`,
        },
      ]
    : []),
])
const selectedLibType = computed(() => libTypes.value.find((it) => it.value === props.libraryId))

const selectedRoute = computed(() => props.routes.find((it) => it.to === currentRoute.path))
</script>

<style scoped></style>
