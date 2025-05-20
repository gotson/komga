<template>
  <v-list-group value="Server">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        title="Server"
        prepend-icon="mdi-cog"
      />
    </template>

    <v-list-item
      to="/server/users"
      title="Users"
    />
    <v-list-item
      to="/server/settings"
      title="Settings"
    />
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
    />
    <v-list-item
      to="/server/updates"
      title="Updates"
    />
  </v-list-group>
</template>
