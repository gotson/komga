<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ $t('dialog.delete_user.dialog_title') }}</v-card-title>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col v-html="$t('dialog.delete_user.warning_html', {name: user.email})"></v-col>
          </v-row>

          <v-row>
            <v-col>
              <v-checkbox v-model="confirmDelete" color="red">
                <template v-slot:label>{{ $t('dialog.delete_user.confirm_delete', {name: user.email}) }}</template>
              </v-checkbox>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ $t('dialog.delete_user.button_cancel') }}</v-btn>
        <v-btn color="error"
               @click="dialogConfirm"
               :disabled="!confirmDelete"
        >{{ $t('dialog.delete_user.button_confirm') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {ERROR} from "@/types/events";

export default Vue.extend({
  name: 'UserDeleteDialog',
  data: () => {
    return {
      confirmDelete: false,
      modal: false,
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
  },
  methods: {
    dialogCancel() {
      this.$emit('input', false)
      this.confirmDelete = false
    },
    dialogConfirm() {
      this.deleteUser()
      this.$emit('input', false)
    },
    async deleteUser() {
      try {
        await this.$store.dispatch('deleteUser', this.user)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
