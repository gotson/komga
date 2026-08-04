<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'Drawer menu for Libraries',
        defaultMessage: 'Libraries',
        id: 'eyYZUe',
      })
    "
    prepend-icon="i-mdi:bookshelf"
    to="/libraries/pinned"
  >
    <template #append>
      <v-icon-btn
        v-if="isAdmin"
        v-tooltip:bottom="
          $formatMessage({
            description: 'Add library button: tooltip',
            defaultMessage: 'Create library',
            id: '70/wK4',
          })
        "
        icon="i-mdi:plus"
        variant="text"
        :aria-label="
          $formatMessage({
            description: 'Add library button: aria label',
            defaultMessage: 'add library',
            id: '90yqRq',
          })
        "
        @mouseenter="(event: Event) => (activator = event.currentTarget as Element)"
        @click.prevent="createLibrary"
      />
      <v-icon-btn
        :id="id"
        icon="i-mdi:dots-vertical"
        variant="text"
        :aria-label="
          $formatMessage({
            description: 'Libraries menu button: aria label',
            defaultMessage: 'libraries menu',
            id: 'hJEc5M',
          })
        "
        @click.prevent="bottomSheet = true"
      />
      <LibraryMenuSheetLibraries
        v-model="bottomSheet"
        :activator="`#${id}`"
      />
    </template>
  </v-list-item>

  <LayoutAppDrawerMenuLibraryItem
    v-for="library in pinned"
    :key="library.id"
    :library="library"
  />

  <v-list-group
    v-if="unpinned.length > 0"
    value="Unpinned"
  >
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        prepend-icon="blank"
        :title="
          $formatMessage({
            description: 'Drawer menu for Unpinned libraries',
            defaultMessage: 'More',
            id: 'XDV3Si',
          })
        "
      />
    </template>

    <LayoutAppDrawerMenuLibraryItem
      v-for="library in unpinned"
      :key="library.id"
      :library="library"
    />
  </v-list-group>
</template>

<script setup lang="ts">
import { useLibraries } from '@/colada/libraries'
import { useCurrentUser } from '@/colada/users'
import { useCreateLibraryDialog } from '@/composables/library/useCreateLibraryDialog'

const { unpinned, pinned, refresh } = useLibraries()
const { isAdmin } = useCurrentUser()

const id = useId()
const bottomSheet = ref(false)

// ensure freshness, especially if libraries have been reordered
void refresh()

const { activator, prepareDialog: createLibrary } = useCreateLibraryDialog()
</script>
