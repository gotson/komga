<template>
  <v-list-group
    ref="group"
    value="Server"
  >
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        title="Server"
        prepend-icon="mdi-cog"
      >
        <template #prepend>
          <v-badge
            :model-value="unreadCount > 0 && !($refs as any).group.isOpen"
            dot
            floating
            color="info"
          >
            <v-icon>mdi-cog</v-icon>
          </v-badge>
        </template>
      </v-list-item>
    </template>

    <v-list-item
      to="/server/users"
      title="Users"
    />
    <v-list-item
      to="/server/settings"
    >
      <v-list-item-title>
        {{
          $formatMessage({
            description: 'Drawer menu for Server Settings',
            defaultMessage: 'Settings',
            id: '9yKJ2S'
          })
        }}
      </v-list-item-title>
    </v-list-item>
    <v-list-item
      to="/server/ui"
      title="User Interface"
    />
    <v-list-item
      to="/server/metrics"
      title="Metrics"
    />

    <v-list-item
      to="/server/announcements"
      title="Announcements"
    >
      <template #append>
        <v-badge
          :model-value="unreadCount > 0"
          :content="unreadCount"
          inline
          color="info"
        />
      </template>
    </v-list-item>

    <v-list-item
      to="/server/updates"
      title="Updates"
    />
  </v-list-group>
</template>

<script setup lang="ts">
import {useAnnouncements} from '@/colada/queries/announcements.ts'

const {unreadCount} = useAnnouncements()
</script>
