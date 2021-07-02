<template>
  <div>
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
                <v-checkbox
                  v-model="roles"
                  :label="$t('dialog.add_user.field_role_administrator')"
                  :value="UserRoles.ADMIN"
                  hide-details
                />
                <v-checkbox
                  v-model="roles"
                  :label="$t('dialog.add_user.field_role_page_streaming')"
                  :value="UserRoles.PAGE_STREAMING"
                  hide-details
                />
                <v-checkbox
                  v-model="roles"
                  :label="$t('dialog.add_user.field_role_file_download')"
                  :value="UserRoles.FILE_DOWNLOAD"
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
          >{{ $t('dialog.edit_user.button_confirm') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >{{ $t('common.close') }}</v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'

export default Vue.extend({
  name: 'UserEditDialog',
  data: () => {
    return {
      UserRoles,
      snackbar: false,
      snackText: '',
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
    value (val) {
      this.modal = val
    },
    modal (val) {
      !val && this.dialogCancel()
    },
    user (val) {
      this.dialogReset(val)
    },
  },
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
  },
  methods: {
    dialogReset (user: UserDto) {
      this.roles = user.roles
    },
    dialogCancel () {
      this.$emit('input', false)
      this.dialogReset(this.user)
    },
    dialogConfirm () {
      this.editUser()
      this.$emit('input', false)
      this.dialogReset(this.user)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async editUser () {
      try {
        const roles = {
          roles: this.roles,
        } as RolesUpdateDto

        await this.$store.dispatch('updateUserRoles', { userId: this.user.id, roles: roles })
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
