<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col><span class="text-h5">{{ $t('server.server_management.section_title') }}</span></v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn @click="modalStopServer = true"
               color="error"
        >{{ $t('server.server_management.button_shutdown') }}</v-btn>
      </v-col>
    </v-row>

    <confirmation-dialog
      v-model="modalStopServer"
      :title="$t('dialog.server_stop.dialog_title')"
      :body="$t('dialog.server_stop.confirmation_message')"
      :button-confirm="$t('dialog.server_stop.button_confirm')"
      button-confirm-color="error"
    />

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >{{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ConfirmationDialog from "@/components/dialogs/ConfirmationDialog.vue";

export default Vue.extend({
  name: 'SettingsServer',
  components: {ConfirmationDialog},
  data: () => ({
    modalStopServer: false,
    snackbar: false,
    snackText: '',
  }),
  methods: {
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async stopServer() {
      try {
        await this.$actuator.shutdown()
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
