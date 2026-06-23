<template>
  <LayoutAppBarHolder />

  <!-- xs only: drawer comes first and will appear over the app bar  -->
  <LayoutAppDrawer :order="display.xs.value ? -1 : 0" />

  <v-main scrollable>
    <RouterView />
  </v-main>
</template>

<script lang="ts" setup>
import { useDisplay } from 'vuetify'

const display = useDisplay()
</script>
