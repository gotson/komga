<template>
  <v-dialog v-model="modalAddUser"
            :fullscreen="this.$vuetify.breakpoint.xsOnly"
            :hide-overlay="this.$vuetify.breakpoint.xsOnly"
            max-width="450"
  >
    <v-card>
      <v-toolbar class="hidden-sm-and-up">
        <v-btn icon @click="dialogCancel">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>{{ dialogTitle }}</v-toolbar-title>
        <v-spacer/>
        <v-toolbar-items>
          <v-btn text color="primary" @click="dialogConfirm">{{ confirmText }}</v-btn>
        </v-toolbar-items>
      </v-toolbar>

      <v-card-title class="hidden-xs-only">{{ dialogTitle }}</v-card-title>

      <v-card-text>

        <form novalidate>
          <v-container fluid>
            <v-row>
              <v-col>
                <v-text-field v-model="form.email"
                              autofocus
                              :label="$t('dialog.add_user.field_email')"
                              :error-messages="getErrors('email')"
                              @blur="$v.form.email.$touch()"
                />
              </v-col>
            </v-row>

            <v-row>
              <v-col>
                <v-text-field v-model="form.password"
                              :label="$t('dialog.add_user.field_password')"
                              autocomplete="off"
                              :type="showPassword ? 'text' : 'password'"
                              :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                              @click:append="showPassword = !showPassword"
                              :error-messages="getErrors('password')"
                              @input="$v.form.password.$touch()"
                              @blur="$v.form.password.$touch()"
                />
              </v-col>
            </v-row>

            <v-row>
              <v-col>
                <span>{{ $t('common.roles') }}</span>
                <v-checkbox v-for="role in userRoles" :key="role.value"
                            v-model="form.roles"
                            :label="role.text"
                            :value="role.value"
                            hide-details
                />
              </v-col>
            </v-row>
          </v-container>
        </form>

      </v-card-text>

      <v-card-actions class="hidden-xs-only">
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ $t('dialog.add_user.button_cancel') }}</v-btn>
        <v-btn color="primary" @click="dialogConfirm">{{ confirmText }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'
import {email, required} from 'vuelidate/lib/validators'
import {ERROR} from '@/types/events'

export default Vue.extend({
  name: 'UserAddDialog',
  data: function () {
    return {
      modalAddUser: true,
      showPassword: false,
      dialogTitle: this.$i18n.t('dialog.add_user.dialog_title').toString(),
      confirmText: this.$i18n.t('dialog.add_user.button_confirm').toString(),
      form: {
        email: '',
        password: '',
        roles: [UserRoles.PAGE_STREAMING, UserRoles.FILE_DOWNLOAD],
      },
    }
  },
  watch: {
    modalAddUser(val) {
      !val && this.dialogCancel()
    },
  },
  validations: {
    form: {
      email: {required, email},
      password: {required},
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
    getErrors(fieldName: string): string[] {
      const errors = [] as string[]

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        if (!field.required) errors.push(this.$t('common.required').toString())
        if (!field.email) errors.push(this.$t('dialog.add_user.field_email_error').toString())
      }
      return errors
    },
    dialogCancel() {
      this.$router.push({name: 'settings-users'})
    },
    dialogConfirm() {
      this.addUser()
    },
    validateUser() {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          email: this.form.email,
          password: this.form.password,
          roles: this.form.roles,
        }
      }
      return null
    },
    async addUser() {
      const user = this.validateUser()
      if (user) {
        try {
          await this.$store.dispatch('postUser', user)
          this.$router.push({name: 'settings-users'})
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
