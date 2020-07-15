<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title>Edit shared libraries</v-card-title>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col>
                <span class="subtitle-1">Shared with {{ this.user.email }}</span>
              </v-col>
            </v-row>

            <v-row>
              <v-col>
                <v-checkbox v-model="allLibraries"
                            label="All libraries"
                            hide-details
                            class="my-0 py-0"
                />
              </v-col>
            </v-row>

            <v-divider/>

            <v-row v-for="(l, index) in libraries" :key="index">
              <v-col>
                <v-checkbox v-model="selectedLibraries"
                            :label="l.name"
                            :value="l.id"
                            :disabled="allLibraries"
                            hide-details
                            class="my-0 py-0"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">Cancel</v-btn>
          <v-btn text class="primary--text"
                 @click="dialogConfirm"
          >Save changes
          </v-btn>
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

export default Vue.extend({
  name: 'UserSharedLibrariesEditDialog',
  data: () => {
    return {
      snackbar: false,
      snackText: '',
      modal: false,
      allLibraries: true,
      selectedLibraries: [] as string[],
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
    dialogReset (user: UserWithSharedLibrariesDto) {
      this.allLibraries = user.sharedAllLibraries
      if (user.sharedAllLibraries) {
        this.selectedLibraries = this.libraries.map(x => x.id)
      } else {
        this.selectedLibraries = user.sharedLibraries.map(x => x.id)
      }
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
        const sharedLibraries = {
          all: this.allLibraries,
          libraryIds: this.selectedLibraries,
        } as SharedLibrariesUpdateDto

        await this.$store.dispatch('updateUserSharedLibraries', { user: this.user, sharedLibraries: sharedLibraries })
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
