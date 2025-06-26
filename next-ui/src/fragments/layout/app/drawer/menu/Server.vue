<template>
  <v-list-group value="Server">
    <template #activator="{ props, isOpen }">
      <v-list-item
        v-bind="props"
        :title="
          $formatMessage({
            description: 'Drawer menu for Server',
            defaultMessage: 'Server',
            id: 'IpvWiZ',
          })
        "
      >
        <template #prepend>
          <v-badge
            :model-value="unreadCount > 0 && !isOpen"
            dot
            floating
            color="info"
          >
            <v-icon icon="i-mdi:cog"></v-icon>
          </v-badge>
        </template>
      </v-list-item>
    </template>

    <v-list-item
      to="/server/users"
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > Users',
          defaultMessage: 'Users',
          id: 'JGOfZq',
        })
      "
    />
    <v-list-item
      to="/server/settings"
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > Settings',
          defaultMessage: 'Settings',
          id: 'HaWCi3',
        })
      "
    />
    <v-list-item
      to="/server/ui"
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > User Interface',
          defaultMessage: 'User Interface',
          id: 'Yf4DJ2',
        })
      "
    />
    <v-list-item
      to="/server/metrics"
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > Metrics',
          defaultMessage: 'Metrics',
          id: '2g7iOx',
        })
      "
    />

    <v-list-item
      to="/server/announcements"
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > Announcements',
          defaultMessage: 'Announcements',
          id: 'G7quju',
        })
      "
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
      :title="
        $formatMessage({
          description: 'Drawer menu for Server > Updates',
          defaultMessage: 'Updates',
          id: 'lDnmZD',
        })
      "
    />
  </v-list-group>
</template>

<script setup lang="ts">
import { useAnnouncements } from '@/colada/queries/announcements'

const { unreadCount } = useAnnouncements()
</script>
