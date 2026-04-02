<template>
  <v-app-bar>
    <template #prepend>
      <span
        v-if="isSingle"
        class="ms-4 text-title-large"
        >{{ title }}</span
      >

      <v-select
        v-else
        :model-value="libraryId"
        :items="selectItems"
        variant="plain"
        class="ms-4"
        @update:model-value="navigate"
      >
        <template #selection="{ item }">
          <span class="text-title-large">{{ item.title }}</span>
        </template>
      </v-select>
    </template>

    <template #default>
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

    <!-- Occupies space, effectively centering the tabs   -->
    <template #append></template>
  </v-app-bar>
</template>

<script setup lang="ts">
import type { Route } from '@/types/route'
import type { LibraryId } from '@/types/libraries'
import { useGetLibrariesById } from '@/composables/libraries'
import { useIntl } from 'vue-intl'

const { routes, libraryId } = defineProps<{
  routes: Route[]
  libraryId: LibraryId
}>()

const intl = useIntl()
const router = useRouter()

const { isSingle, libraries } = useGetLibrariesById(libraryId)

const title = computed(() => (isSingle.value ? libraries.value?.[0]?.name : undefined))

const selectItems = [
  {
    title: intl.formatMessage({
      description: 'Library tab navigation: library selection: pinned',
      defaultMessage: 'Pinned',
      id: '1qIfds',
    }),
    value: 'pinned',
  },
  {
    title: intl.formatMessage({
      description: 'Library tab navigation: library selection: unpinned',
      defaultMessage: 'Unpinned',
      id: '9oA9gw',
    }),
    value: 'unpinned',
  },
  {
    title: intl.formatMessage({
      description: 'Library tab navigation: library selection: all',
      defaultMessage: 'All',
      id: '8/BXfN',
    }),
    value: 'all',
  },
]

function navigate(id: string) {
  void router.push({
    name: '/libraries/[id]',
    params: { id: id },
  })
}
</script>

<style scoped></style>
