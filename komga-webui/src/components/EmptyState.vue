<template>
  <div class="text-center">
    <v-avatar color="grey lighten-3" :size="size">
      <div>
        <v-icon v-if="icon !== ''"
                :color="iconColor"
                :size="iconSize"
        >{{ icon }}
        </v-icon>
        <h1 class="headline">{{ title }}</h1>
        <p class="body-1">{{ subTitle }}</p>
        <slot/>
      </div>
    </v-avatar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'EmptyState',
  props: {
    title: String,
    subTitle: String,
    icon: String,
    iconColor: String,
    iconSize: {
      type: [Number, String],
      default: 140
    },
    size: {
      type: [Number, String],
      default: 400
    }
  }
})
</script>

<style scoped>

</style>
