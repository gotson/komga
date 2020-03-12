<template>
  <div>
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
                                label="Email"
                                :error-messages="getErrors('email')"
                                @blur="$v.form.email.$touch()"
                  />
                </v-col>
              </v-row>

              <v-row>
                <v-col>
                  <v-text-field v-model="form.password"
                                label="Password"
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
                  <span>Roles</span>
                  <v-checkbox
                    v-model="form.admin"
                    label="Administrator"
                  />
                </v-col>
              </v-row>
            </v-container>
          </form>

        </v-card-text>

        <v-card-actions class="hidden-xs-only">
          <v-spacer/>
          <v-btn text @click="dialogCancel">Cancel</v-btn>
          <v-btn text class="primary--text" @click="dialogConfirm">{{ confirmText }}</v-btn>
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
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { email, required } from 'vuelidate/lib/validators'

export default Vue.extend({
  name: 'UserAddDialog',
  data: () => {
    return {
      modalAddUser: true,
      showPassword: false,
      snackbar: false,
      snackText: '',
      dialogTitle: 'Add User',
      confirmText: 'Add',
      form: {
        email: '',
        password: '',
        admin: false
      },
      validationFieldNames: new Map([])
    }
  },
  watch: {
    modalAddUser (val) {
      !val && this.dialogCancel()
    }
  },
  validations: {
    form: {
      email: { required, email },
      password: { required }
    }
  },
  methods: {
    getErrors (fieldName: string): string[] {
      const errors = [] as string[]

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        const properName = this.validationFieldNames.has(fieldName)
          ? this.validationFieldNames.get(fieldName) : fieldName.charAt(0).toUpperCase() + fieldName.substring(1)
        if (!field.required) errors.push(`${properName} is required.`)
        if (!field.email) errors.push(`${properName} must be a valid email address.`)
      }
      return errors
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    dialogCancel () {
      this.$router.push({ name: 'settings-users' })
    },
    dialogConfirm () {
      this.addUser()
    },
    validateUser () {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          email: this.form.email,
          password: this.form.password,
          roles: this.form.admin ? ['ADMIN'] : []
        }
      }
      return null
    },
    async addUser () {
      const user = this.validateUser()
      if (user) {
        try {
          await this.$store.dispatch('postUser', user)
          this.$router.push({ name: 'settings-users' })
        } catch (e) {
          this.showSnack(e.message)
        }
      }
    }
  }
})
</script>

<style scoped>

</style>
