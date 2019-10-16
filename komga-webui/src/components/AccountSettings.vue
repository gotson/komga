<template>
  <v-container fluid>
    <v-row>
      <span class="headline">Account Settings</span>
    </v-row>
    <v-row align="center">
      <v-col cols="2">Email</v-col>
      <v-col>
        <v-text-field readonly v-model="me.email"></v-text-field>
      </v-col>
    </v-row>
    <v-row align="center">
      <v-col cols="2">Roles</v-col>
      <v-col>
        <v-chip-group>
          <v-chip v-for="role in me.roles" :key="role">{{ role }}</v-chip>
        </v-chip-group>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn color="primary"
               @click.prevent="modalPasswordChange = true"
        >Change password
        </v-btn>
      </v-col>
    </v-row>

    <password-change-dialog v-model="modalPasswordChange"
                            :user="me"
    ></password-change-dialog>

  </v-container>
</template>

<script lang="ts">
import PasswordChangeDialog from '@/components/PasswordChangeDialog.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'AccountSettings',
  components: { PasswordChangeDialog },
  data: () => {
    return {
      modalPasswordChange: false,
      newPassword: ''
    }
  },
  computed: {
    me (): UserDto {
      return this.$store.state.komgaUsers.me
    }
  }
})
</script>

<style scoped>

</style>
