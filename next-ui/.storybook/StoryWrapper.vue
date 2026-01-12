<template>
  <v-app
    :theme="themeName"
    class="pa-2"
  >
    <v-main>
      <slot name="story"></slot>
    </v-main>
  </v-app>
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

.v-application__wrap {
  min-height: unset !important;
  height: auto !important;
}
</style>
