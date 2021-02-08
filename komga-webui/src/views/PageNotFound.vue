<template>
  <v-row justify="center">
    <empty-state :title="$t('page_not_found.page_not_found')"
                 :sub-title="$t('page_not_found.page_does_not_exist')"
                 icon="mdi-help-circle"
                 icon-color="secondary"
    >
      <v-btn color="primary" :to="{name: 'home'}">{{ $t('page_not_found.go_back_to_home_page') }}</v-btn>
    </empty-state>
  </v-row>
</template>

<script lang="ts">
import EmptyState from '@/components/EmptyState.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'PageNotFound',
  components: { EmptyState },
})
</script>

<style scoped>

</style>
