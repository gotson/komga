<template>
  <v-list-group
    ref="group"
    value="Server"
  >
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        :title="$formatMessage({
          description: 'Drawer menu for Server',
          defaultMessage: 'Server',
          id: 'IpvWiZ'
        })"
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
      :title="$formatMessage({
        description: 'Drawer menu for Users',
        defaultMessage: 'Users',
        id: 'BXkzdX'
      })"
    />
    <v-list-item
      to="/server/settings"
      :title="$formatMessage({
        description: 'Drawer menu for Server Settings',
        defaultMessage: 'Settings',
        id: '9yKJ2S'
      })"
    />
    <v-list-item
      to="/server/ui"
      :title="$formatMessage({
        description: 'Drawer menu for User Interface',
        defaultMessage: 'User Interface',
        id: '8dsA6c'
      })"
    />
    <v-list-item
      to="/server/metrics"
      :title="$formatMessage({
        description: 'Drawer menu for Metrics',
        defaultMessage: 'Metrics',
        id: 'nr3gEG'
      })"
    />

    <v-list-item
      to="/server/announcements"
      :title="$formatMessage({
        description: 'Drawer menu for Announcements',
        defaultMessage: 'Announcements',
        id: 'Nn5L5Q'
      })"
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
      :title="$formatMessage({
        description: 'Drawer menu for Updates',
        defaultMessage: 'Updates',
        id: 'nGrxDH'
      })"
    />
  </v-list-group>
</template>

<script setup lang="ts">
import {useAnnouncements} from '@/colada/queries/announcements.ts'

const {unreadCount} = useAnnouncements()
</script>
