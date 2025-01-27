<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col cols="12" md="10" lg="8" xl="4">
        <api-key-table/>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ApiKeyTable from '@/components/ApiKeyTable.vue'

export default Vue.extend({
  name: 'ApiKeys',
  components: {ApiKeyTable},
})
</script>

<style scoped>

</style>
