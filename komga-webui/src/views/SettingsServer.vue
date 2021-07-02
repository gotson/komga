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
      @confirm="stopServer"
    />

  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ConfirmationDialog from "@/components/dialogs/ConfirmationDialog.vue";
import {ERROR} from "@/types/events";

export default Vue.extend({
  name: 'SettingsServer',
  components: {ConfirmationDialog},
  data: () => ({
    modalStopServer: false,
  }),
  methods: {
    async stopServer() {
      try {
        await this.$actuator.shutdown()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
