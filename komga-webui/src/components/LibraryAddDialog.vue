<template>
  <div>
    <v-dialog v-model="modalAddLibrary"
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
                  <v-text-field v-model="form.name"
                                label="Name"
                                :error-messages="getErrors('name')"
                                @input="$v.form.name.$touch()"
                                @blur="$v.form.name.$touch()"
                  />
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="8">
                  <file-browser-dialog
                    v-model="modalFileBrowser"
                    :path.sync="form.path"
                    confirm-text="Choose"
                    dialog-title="Library's root folder"
                  />

                  <v-text-field v-model="form.path"
                                label="Root folder"
                                disabled
                                :error-messages="getErrors('path')"
                                @input="$v.form.path.$touch()"
                                @blur="$v.form.path.$touch()"
                  />
                </v-col>
                <v-col cols="4">
                  <v-btn @click="modalFileBrowser = true">Browse</v-btn>
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
import FileBrowserDialog from '@/components/FileBrowserDialog.vue'
import { required } from 'vuelidate/lib/validators'

export default Vue.extend({
  name: 'LibraryAddDialog',
  components: { FileBrowserDialog },
  data: () => {
    return {
      modalAddLibrary: true,
      modalFileBrowser: false,
      snackbar: false,
      snackText: '',
      dialogTitle: 'Add Library',
      confirmText: 'Add',
      form: {
        name: '',
        path: ''
      },
      validationFieldNames: new Map([])
    }
  },
  watch: {
    modalAddLibrary (val) {
      !val && this.dialogCancel()
    }
  },
  validations: {
    form: {
      name: { required },
      path: { required }
    }
  },
  methods: {
    getErrors (fieldName: string) {
      const errors = []

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        const properName = this.validationFieldNames.has(fieldName)
          ? this.validationFieldNames.get(fieldName) : fieldName.charAt(0).toUpperCase() + fieldName.substring(1)
        errors.push(`${properName} is required.`)
      }
      return errors
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    dialogCancel () {
      this.$router.back()
    },
    dialogConfirm () {
      this.addLibrary()
    },
    validateLibrary () {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          name: this.form.name,
          root: this.form.path
        }
      }
      return null
    },
    async addLibrary () {
      const library = this.validateLibrary()
      if (library) {
        try {
          await this.$store.dispatch('postLibrary', library)
          this.$router.push({ name: 'home' })
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
