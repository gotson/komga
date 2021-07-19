<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col class="text-h5">{{ $t('users.users') }}</v-col>
    </v-row>
    <v-row>
      <v-col cols="12" md="10" lg="8" xl="4">
        <users-list/>
      </v-col>
    </v-row>

    <v-row>
      <v-col class="text-h5">{{ $t('users.authentication_activity') }}</v-col>
    </v-row>
    <v-row>
      <v-col cols="12">
        <authentication-activity-table/>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import AuthenticationActivityTable from '@/components/AuthenticationActivityTable.vue'
import UsersList from '@/components/UsersList.vue'

export default Vue.extend({
  name: 'SettingsUsers',
  components: {UsersList, AuthenticationActivityTable  },
})
</script>

<style scoped>

</style>
