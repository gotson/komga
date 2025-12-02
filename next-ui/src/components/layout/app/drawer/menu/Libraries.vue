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
  >
    <template #append>
      <v-icon-btn
        v-if="isAdmin"
        icon="i-mdi:plus"
        :aria-label="
          $formatMessage({
            description: 'Add library button: aria label',
            defaultMessage: 'add library',
            id: '90yqRq',
          })
        "
      />
      <v-icon-btn
        icon="i-mdi:dots-vertical"
        :aria-label="
          $formatMessage({
            description: 'Libraries menu button: aria label',
            defaultMessage: 'libraries menu',
            id: 'hJEc5M',
          })
        "
      />
    </template>
  </v-list-item>

  <v-list-item
    v-for="library in pinned"
    :key="library.id"
    :title="library.name"
    prepend-icon="blank"
  >
    <template #append>
      <v-icon-btn
        v-if="isAdmin"
        icon="i-mdi:dots-vertical"
        :aria-label="
          $formatMessage({
            description: 'Library menu button: aria label',
            defaultMessage: 'library menu',
            id: '3gimvl',
          })
        "
      />
    </template>
  </v-list-item>

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

    <v-list-item
      v-for="library in unpinned"
      :key="library.id"
      :title="library.name"
      prepend-icon="blank"
    >
      <template #append>
        <v-icon-btn
          v-if="isAdmin"
          icon="i-mdi:dots-vertical"
          :aria-label="
            $formatMessage({
              description: 'Library menu button: aria label',
              defaultMessage: 'library menu',
              id: '3gimvl',
            })
          "
        />
      </template>
    </v-list-item>
  </v-list-group>
</template>

<script setup lang="ts">
import { useLibraries } from '@/colada/libraries'
import { useCurrentUser } from '@/colada/users'

const { unpinned, pinned } = useLibraries()
const { isAdmin } = useCurrentUser()
</script>
