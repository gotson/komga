<template>
  <AppBar />

  <AppDrawer />

  <v-main scrollable>
    <v-container
      fluid
      class="pa-6"
    >
      <router-view />
    </v-container>
  </v-main>

  <AppFooter />
</template>

<script lang="ts" setup>

</script>
