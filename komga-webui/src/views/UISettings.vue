<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col cols="auto">
        <v-checkbox
          v-model="form.oauth2HideLogin"
          @change="$v.form.oauth2HideLogin.$touch()"
          :label="$t('ui_settings.label_oauth2_hide_login')"
          hide-details
        />
        <v-checkbox
          v-model="form.oauth2AutoLogin"
          @change="$v.form.oauth2AutoLogin.$touch()"
          :label="$t('ui_settings.label_oauth2_auto_login')"
          hide-details
        >
          <template v-slot:append>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on">
                  mdi-information-outline
                </v-icon>
              </template>
              {{ $t('ui_settings.tooltip_oauth2_auto_login') }}
            </v-tooltip>
          </template>

        </v-checkbox>

        <v-checkbox
          v-model="form.posterStretch"
          @change="$v.form.posterStretch.$touch()"
          :label="$t('ui_settings.label_poster_stretch')"
          hide-details
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="auto">
        <v-btn @click="refreshSettings"
               :disabled="discardDisabled"
        >{{ $t('common.discard') }}
        </v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary"
               :disabled="saveDisabled"
               @click="saveSettings"
        >{{ $t('common.save_changes') }}
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'
import {CLIENT_SETTING, ClientSettingDto} from '@/types/komga-clientsettings'

export default Vue.extend({
  name: 'UISettings',
  components: {FileBrowserDialog, ConfirmationDialog},
  data: () => ({
    form: {
      oauth2HideLogin: false,
      oauth2AutoLogin: false,
      posterStretch: false,
    },
    existingSettings: [] as ClientSettingDto[],
  }),
  validations: {
    form: {
      oauth2HideLogin: {},
      oauth2AutoLogin: {},
      posterStretch: {},
    },
  },
  mounted() {
    this.refreshSettings()
  },
  computed: {
    saveDisabled(): boolean {
      return this.$v.form.$invalid || !this.$v.form.$anyDirty
    },
    discardDisabled(): boolean {
      return !this.$v.form.$anyDirty
    },
  },
  methods: {
    async refreshSettings() {
      await this.$store.dispatch('getClientSettings')
      this.form.oauth2HideLogin = this.$store.getters.getClientSettingByKey(CLIENT_SETTING.WEBUI_OAUTH2_HIDE_LOGIN)?.value === 'true'
      this.form.oauth2AutoLogin = this.$store.getters.getClientSettingByKey(CLIENT_SETTING.WEBUI_OAUTH2_AUTO_LOGIN)?.value === 'true'
      this.form.posterStretch = this.$store.getters.getClientSettingByKey(CLIENT_SETTING.WEBUI_POSTER_STRETCH)?.value === 'true'
      this.$v.form.$reset()
    },
    async saveSettings() {
      if (this.$v.form?.oauth2HideLogin?.$dirty)
        await this.$komgaSettings.updateClientSettingGlobal({
          key: CLIENT_SETTING.WEBUI_OAUTH2_HIDE_LOGIN,
          value: this.form.oauth2HideLogin ? 'true' : 'false',
          allowUnauthorized: true,
        })

      if (this.$v.form?.oauth2AutoLogin?.$dirty)
        await this.$komgaSettings.updateClientSettingGlobal({
          key: CLIENT_SETTING.WEBUI_OAUTH2_AUTO_LOGIN,
          value: this.form.oauth2AutoLogin ? 'true' : 'false',
          allowUnauthorized: true,
        })

      if (this.$v.form?.posterStretch?.$dirty)
        await this.$komgaSettings.updateClientSettingGlobal({
          key: CLIENT_SETTING.WEBUI_POSTER_STRETCH,
          value: this.form.posterStretch ? 'true' : 'false',
          allowUnauthorized: false,
        })

      await this.refreshSettings()
    },
  },
})
</script>

<style scoped>

</style>
