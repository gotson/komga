<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col>
        <server-settings/>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <server-management/>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ServerSettings from '@/views/ServerSettings.vue'
import ServerManagement from '@/views/ServerManagement.vue'

export default Vue.extend({
  name: 'SettingsServer',
  components: {ServerManagement, ServerSettings},
})
</script>

<style scoped>

</style>
