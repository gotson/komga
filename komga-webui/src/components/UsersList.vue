<template>
  <div style="position: relative">
    <v-list
      elevation="3"
      two-line
    >
      <div v-for="(u, index) in users" :key="index">
        <v-list-item
        >
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-list-item-icon v-on="on">
                <v-icon v-if="u.roles.includes(UserRoles.ADMIN)" color="red">mdi-account-star</v-icon>
                <v-icon v-else>mdi-account</v-icon>
              </v-list-item-icon>
            </template>
            <span>{{
                u.roles.includes(UserRoles.ADMIN) ? $t('settings_user.role_administrator') : $t('settings_user.role_user')
              }}</span>
          </v-tooltip>

          <v-list-item-content>
            <v-list-item-title>
              {{ u.email }}
            </v-list-item-title>
            <v-list-item-subtitle v-if="usersLastActivity[u.id] !== undefined">
              {{
                $t('settings_user.latest_activity', {
                  date:
                    new Intl.DateTimeFormat($i18n.locale, {
                      dateStyle: 'medium',
                      timeStyle: 'short'
                    }).format(usersLastActivity[u.id])
                })
              }}
            </v-list-item-subtitle>
            <v-list-item-subtitle v-else>{{ $t('settings_user.no_recent_activity') }}</v-list-item-subtitle>
          </v-list-item-content>

          <v-list-item-action>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-btn icon @click="editRestrictions(u)" :disabled="u.roles.includes(UserRoles.ADMIN)"
                       v-on="on">
                  <v-icon>mdi-book-lock</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('settings_user.edit_restrictions') }}</span>
            </v-tooltip>
          </v-list-item-action>

          <v-list-item-action>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-btn icon @click="changeUserPassword(u)" v-on="on">
                  <v-icon>mdi-lock-reset</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('settings_user.change_password') }}</span>
            </v-tooltip>
          </v-list-item-action>

          <v-list-item-action>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-btn icon @click="editUser(u)" :disabled="u.id === me.id" v-on="on">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('settings_user.edit_user') }}</span>
            </v-tooltip>
          </v-list-item-action>

          <v-list-item-action>
            <v-btn icon @click="promptDeleteUser(u)"
                   :disabled="u.id === me.id"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>

        <v-divider v-if="index !== users.length-1"/>
      </div>
    </v-list>

    <v-btn fab absolute bottom color="primary"
           :right="!$vuetify.rtl"
           :left="$vuetify.rtl"
           class="mx-6"
           small
           :to="{name: 'settings-users-add'}">
      <v-icon>mdi-plus</v-icon>
    </v-btn>

    <user-restrictions-edit-dialog v-model="modalEditRestrictions"
                                       :user="userToEditRestrictions"
    />

    <password-change-dialog v-model="modalChangePassword"
                            :user="userToChangePassword"
    />

    <user-edit-dialog v-model="modalEditUser"
                      :user="userToEdit"
    />

    <confirmation-dialog
      v-model="modalDeleteUser"
      :title="$t('dialog.delete_user.dialog_title')"
      :body-html="$t('dialog.delete_user.warning_html', {name: userToDelete.email})"
      :confirm-text=" $t('dialog.delete_user.confirm_delete', {name: userToDelete.email})"
      :button-confirm="$t('dialog.delete_user.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteUser"
    />

    <router-view/>
  </div>
</template>

<script lang="ts">
import UserEditDialog from '@/components/dialogs/UserEditDialog.vue'
import UserRestrictionsEditDialog from '@/components/dialogs/UserRestrictionsEditDialog.vue'
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'
import PasswordChangeDialog from '@/components/dialogs/PasswordChangeDialog.vue'
import {ERROR} from '@/types/events'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import { UserDto } from '@/types/komga-users'

export default Vue.extend({
  name: 'UsersList',
  components: {
    ConfirmationDialog,
    UserRestrictionsEditDialog,
    UserEditDialog,
    PasswordChangeDialog,
  },
  data: () => ({
    UserRoles,
    modalAddUser: false,
    modalDeleteUser: false,
    userToDelete: {} as UserDto,
    modalEditRestrictions: false,
    userToEditRestrictions: {} as UserDto,
    modalEditUser: false,
    userToEdit: {} as UserDto,
    modalChangePassword: false,
    userToChangePassword: {} as UserDto,
    usersLastActivity: {} as any,
  }),
  computed: {
    users(): UserDto[] {
      return this.$store.state.komgaUsers.users
    },
    me(): UserDto {
      return this.$store.state.komgaUsers.me
    },
  },
  watch: {
    users(val) {
      val.forEach((u: UserDto) => {
        this.$komgaUsers.getLatestAuthenticationActivityForUser(u.id)
          .then(value => this.$set(this.usersLastActivity, `${u.id}`, value.dateTime))
          .catch(e => {
          })
      })
    },
  },
  async mounted() {
    await this.$store.dispatch('getAllUsers')
  },
  methods: {
    promptDeleteUser(user: UserDto) {
      this.userToDelete = user
      this.modalDeleteUser = true
    },
    editRestrictions(user: UserDto) {
      this.userToEditRestrictions = user
      this.modalEditRestrictions = true
    },
    editUser(user: UserDto) {
      this.userToEdit = user
      this.modalEditUser = true
    },
    changeUserPassword(user: UserDto) {
      this.userToChangePassword = user
      this.modalChangePassword = true
    },
    async deleteUser() {
      try {
        await this.$store.dispatch('deleteUser', this.userToDelete)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
