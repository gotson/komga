<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ $t('dialog.edit_user_shared_libraries.dialog_title') }}</v-card-title>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col>
              <span class="text-subtitle-1">{{
                  $t('dialog.edit_user_shared_libraries.label_shared_with', {name: user.email})
                }}</span>
            </v-col>
          </v-row>

          <v-row>
            <v-col>
              <v-checkbox v-model="allLibraries"
                          :label="$t('dialog.edit_user_shared_libraries.field_all_libraries')"
                          hide-details
                          class="my-0 py-0"
              />
            </v-col>
          </v-row>

          <v-divider class="my-2"/>

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
        <v-btn text @click="dialogCancel">{{ $t('dialog.edit_user_shared_libraries.button_cancel') }}</v-btn>
        <v-btn color="primary"
               @click="dialogConfirm"
        >{{ $t('dialog.edit_user_shared_libraries.button_confirm') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {ERROR} from '@/types/events'
import {LibraryDto} from '@/types/komga-libraries'

export default Vue.extend({
  name: 'UserSharedLibrariesEditDialog',
  data: () => {
    return {
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
    libraries(): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
  },
  methods: {
    dialogReset(user: UserWithSharedLibrariesDto) {
      this.allLibraries = user.sharedAllLibraries
      if (user.sharedAllLibraries) {
        this.selectedLibraries = this.libraries.map(x => x.id)
      } else {
        this.selectedLibraries = user.sharedLibraries.map(x => x.id)
      }
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
        const sharedLibraries = {
          all: this.allLibraries,
          libraryIds: this.selectedLibraries,
        } as SharedLibrariesUpdateDto

        await this.$store.dispatch('updateUserSharedLibraries', {user: this.user, sharedLibraries: sharedLibraries})
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
