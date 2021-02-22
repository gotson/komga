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
    <server-stop-dialog v-model="modalStopServer"></server-stop-dialog>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ServerStopDialog from "@/components/dialogs/ServerStopDialog.vue";

export default Vue.extend({
  name: 'SettingsServer',
  components: {ServerStopDialog},
  data: () => ({
    modalStopServer: false,
  }),
})
</script>

<style scoped>

</style>
