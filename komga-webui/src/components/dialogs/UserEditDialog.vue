<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ $t('dialog.edit_user.dialog_title') }}</v-card-title>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col>
              <span class="text-subtitle-1">{{ $t('dialog.edit_user.label_roles_for', {name: user.email}) }}</span>
            </v-col>
          </v-row>

          <v-row>
            <v-col>
              <v-checkbox v-for="role in userRoles" :key="role.value"
                          v-model="roles"
                          :label="role.text"
                          :value="role.value"
                          hide-details
              />
            </v-col>
          </v-row>

        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ $t('dialog.edit_user.button_cancel') }}</v-btn>
        <v-btn color="primary"
               @click="dialogConfirm"
        >{{ $t('dialog.edit_user.button_confirm') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'
import {ERROR} from '@/types/events'
import {UserDto, UserUpdateDto} from '@/types/komga-users'

export default Vue.extend({
  name: 'UserEditDialog',
  data: () => {
    return {
      modal: false,
      roles: [] as string[],
    }
  },
  props: {
    value: Boolean,
    user: {
      type: Object,
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      !val && this.dialogCancel()
    },
    user(val) {
      this.dialogReset(val)
    },
  },
  computed: {
    userRoles(): any[] {
      return Object.keys(UserRoles).map(x => ({
        text: this.$t(`user_roles.${x}`),
        value: x,
      }))
    },
  },
  methods: {
    dialogReset(user: UserDto) {
      this.roles = user.roles
    },
    dialogCancel() {
      this.$emit('input', false)
      this.dialogReset(this.user)
    },
    dialogConfirm() {
      this.editUser()
      this.$emit('input', false)
      this.dialogReset(this.user)
    },
    async editUser() {
      try {
        const patch = {
          roles: this.roles,
        } as UserUpdateDto

        await this.$store.dispatch('updateUser', {userId: this.user.id, patch: patch})
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
