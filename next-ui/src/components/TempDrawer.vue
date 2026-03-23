<template>
  <!--  Teleport is needed so that the scrim covers the whole screen  -->
  <Teleport to="#app">
    <!--  order=-1 is needed for the drawer to open full height  -->
    <!--  disable-route-watcher is needed, else the drawer closes when the route query params are updated when the filters change -->
    <v-navigation-drawer
      v-model="model"
      :location="location"
      temporary
      order="-1"
      disable-route-watcher
    >
      <v-icon-btn
        icon="i-mdi:close"
        variant="text"
        class="position-absolute top-0 right-0 me-2 mt-1"
        style="z-index: 2"
        @click="model = false"
      />
      <slot />
    </v-navigation-drawer>
  </Teleport>
</template>

<script setup lang="ts">
const model = defineModel<boolean>({ default: false })
const { location = 'end' } = defineProps<{
  location?: 'top' | 'end' | 'bottom' | 'start' | 'left' | 'right'
}>()
</script>
