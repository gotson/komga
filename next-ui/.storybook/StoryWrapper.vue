<template>
  <v-theme-provider
    with-background
    :theme="themeName"
    class="pa-2"
  >
    <slot name="story"></slot>
  </v-theme-provider>
</template>

<script lang="ts">
export default {
  props: {
    themeName: String,
  },
}
</script>

<style>
@import '../src/styles/global.scss';
</style>
