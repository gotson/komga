<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col cols="auto">
        <span class="font-weight-black text-h6">{{ $t('ui_settings.general') }}</span>

        <v-radio-group
          v-model="form.seriesGroups"
          @change="$v.form.seriesGroups.$touch()"
          :label="$t('ui_settings.label_series_groups')"
          hide-details
        >
          <v-radio value="alpha" :label="$t('ui_settings.series_groups.alpha')"/>
          <v-radio value="japanese" :label="$t('ui_settings.series_groups.japanese')"/>
        </v-radio-group>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="auto">
        <span class="font-weight-black text-h6">{{ $t('ui_settings.section_oauth2') }}</span>

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
import {
  CLIENT_SETTING,
  ClientSettingGlobalUpdateDto,
  ClientSettingsSeriesGroup,
  SERIES_GROUP_ALPHA,
  SERIES_GROUP_JAPANESE,
} from '@/types/komga-clientsettings'

export default Vue.extend({
  name: 'UISettings',
  data: () => ({
    form: {
      oauth2HideLogin: false,
      oauth2AutoLogin: false,
      seriesGroups: 'alpha',
    },
  }),
  validations: {
    form: {
      oauth2HideLogin: {},
      oauth2AutoLogin: {},
      seriesGroups: {},
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
      await this.$store.dispatch('getClientSettingsGlobal')
      this.form.oauth2HideLogin = this.$store.state.komgaSettings.clientSettingsGlobal[CLIENT_SETTING.WEBUI_OAUTH2_HIDE_LOGIN]?.value === 'true'
      this.form.oauth2AutoLogin = this.$store.state.komgaSettings.clientSettingsGlobal[CLIENT_SETTING.WEBUI_OAUTH2_AUTO_LOGIN]?.value === 'true'
      try {
        this.form.seriesGroups = (JSON.parse(this.$store.state.komgaSettings.clientSettingsGlobal[CLIENT_SETTING.WEBUI_SERIES_GROUPS]?.value) as ClientSettingsSeriesGroup)?.name
      } catch (_) {
      }
      this.$v.form.$reset()
    },
    async saveSettings() {
      let newSettings = {} as Record<string, ClientSettingGlobalUpdateDto>
      if (this.$v.form?.oauth2HideLogin?.$dirty)
        newSettings[CLIENT_SETTING.WEBUI_OAUTH2_HIDE_LOGIN] = {
          value: this.form.oauth2HideLogin ? 'true' : 'false',
          allowUnauthorized: true,
        }

      if (this.$v.form?.oauth2AutoLogin?.$dirty)
        newSettings[CLIENT_SETTING.WEBUI_OAUTH2_AUTO_LOGIN] = {
          value: this.form.oauth2AutoLogin ? 'true' : 'false',
          allowUnauthorized: true,
        }

      if (this.$v.form?.seriesGroups?.$dirty)
        newSettings[CLIENT_SETTING.WEBUI_SERIES_GROUPS] = {
          value: JSON.stringify(this.form.seriesGroups === 'alpha' ? SERIES_GROUP_ALPHA : SERIES_GROUP_JAPANESE),
          allowUnauthorized: false,
        }

      await this.$komgaSettings.updateClientSettingGlobal(newSettings)

      await this.refreshSettings()
    },
  },
})
</script>

<style scoped>

</style>
