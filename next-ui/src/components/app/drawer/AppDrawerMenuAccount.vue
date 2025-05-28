<template>
  <v-list-group value=" My Account">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        :title="$formatMessage({
          description: 'Drawer menu for My Account',
          defaultMessage: 'My Account',
          id: 'od545m'
        })"
        prepend-icon="mdi-account"
      />
    </template>

    <v-list-item
      to="/account/details"
      :title="$formatMessage({
        description: 'Drawer menu for My Account > Details',
        defaultMessage: 'Details',
        id: 'xYGXuU'
      })"
    />
    <v-list-item
      to="/account/api-keys"
      :title="$formatMessage({
        description: 'Drawer menu for My Account > API Keys',
        defaultMessage: 'API Keys',
        id: 'oFOkWZ'
      })"
    />
    <v-list-item
      to="/account/ui"
      :title="$formatMessage({
        description: 'Drawer menu for My Account > User Interface',
        defaultMessage: 'User Interface',
        id: 'rw/Dkw'
      })"
    />
    <v-list-item
      to="/account/activity"
      :title="$formatMessage({
        description: 'Drawer menu for My Account > Activity',
        defaultMessage: 'Activity',
        id: 'cGFtPg'
      })"
    />
  </v-list-group>
</template>

<script setup lang="ts">
</script>

<script lang="ts">
</script>

<style scoped>

</style>
