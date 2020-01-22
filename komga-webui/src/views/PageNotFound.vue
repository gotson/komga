<template>
  <v-row justify="center">
    <empty-state title="Page not found"
                 sub-title="The page you are looking for doesn't exist."
                 icon="mdi-help-circle"
                 icon-color="secondary"
    >
      <v-btn color="primary" :to="{name: 'home'}">Go back to home page</v-btn>
    </empty-state>
  </v-row>
</template>

<script lang="ts">
import EmptyState from '@/components/EmptyState.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'PageNotFound',
  components: { EmptyState }
})
</script>

<style scoped>

</style>
