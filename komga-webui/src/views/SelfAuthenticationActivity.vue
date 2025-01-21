<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col>
        <authentication-activity-table for-me/>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import AuthenticationActivityTable from '@/components/AuthenticationActivityTable.vue'

export default Vue.extend({
  name: 'SelfAuthenticationActivity',
  components: {AuthenticationActivityTable},
})
</script>

<style scoped>

</style>
