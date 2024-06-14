<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col class="text-h5">{{ $t('account_settings.account_settings') }}</v-col>
    </v-row>

    <v-row align="center">
      <v-col cols="12" md="8" lg="6" xl="4">
        <span class="text-capitalize">{{ $t('common.email') }}</span>
        <v-text-field readonly
                      v-model="me.email"
        />
      </v-col>
    </v-row>

    <v-row align="center">
      <v-col>
        <span>{{ $t('common.roles') }}</span>
        <v-chip-group>
          <v-chip v-for="role in me.roles" :key="role"
          >{{ $t(`user_roles.${role}`) }}
          </v-chip>
          <v-chip v-if="me.roles.length === 0">USER</v-chip>
        </v-chip-group>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-btn color="primary"
               @click.prevent="modalPasswordChange = true"
        >{{ $t('account_settings.change_password') }}
        </v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col class="text-h5">{{ $t('users.api_keys') }}</v-col>
    </v-row>

    <v-row>
      <v-col cols="12" md="10" lg="8" xl="4">
        <api-key-table/>
      </v-col>
    </v-row>

    <v-row>
      <v-col class="text-h5">{{ $t('users.authentication_activity') }}</v-col>
    </v-row>

    <v-row>
      <v-col>
        <authentication-activity-table for-me/>
      </v-col>
    </v-row>

    <password-change-dialog v-model="modalPasswordChange"
                            :user="me"
    />

  </v-container>
</template>

<script lang="ts">
import PasswordChangeDialog from '@/components/dialogs/PasswordChangeDialog.vue'
import Vue from 'vue'
import AuthenticationActivityTable from '@/components/AuthenticationActivityTable.vue'
import {UserDto} from '@/types/komga-users'
import ApiKeyTable from '@/components/ApiKeyTable.vue'

export default Vue.extend({
  name: 'AccountSettings',
  components: {ApiKeyTable, AuthenticationActivityTable, PasswordChangeDialog},
  data: () => {
    return {
      modalPasswordChange: false,
      newPassword: '',
    }
  },
  computed: {
    me(): UserDto {
      return this.$store.state.komgaUsers.me
    },
  },
})
</script>

<style scoped>

</style>
