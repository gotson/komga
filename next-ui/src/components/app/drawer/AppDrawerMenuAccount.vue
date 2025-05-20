<template>
  <v-list-group value=" My Account">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        title="My Account"
        prepend-icon="mdi-account"
      />
    </template>

    <v-list-item
      to="/account/details"
      title="Details"
    />
    <v-list-item
      to="/account/api-keys"
      title="API Keys"
    />
    <v-list-item
      to="/account/ui"
      title="User Interface"
    />
    <v-list-item
      to="/account/activity"
      title="Activity"
    />
  </v-list-group>
</template>

<script setup lang="ts">
</script>

<script lang="ts">
</script>

<style scoped>

</style>
