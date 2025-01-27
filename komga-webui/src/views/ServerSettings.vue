<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col><span class="text-h5">{{ $t('server_settings.server_settings') }}</span></v-col>
    </v-row>
    <v-row>
      <v-col cols="auto">
        <v-select
          v-model="form.thumbnailSize"
          @change="$v.form.thumbnailSize.$touch()"
          :items="thumbnailSizes"
          :label="$t('server_settings.label_thumbnail_size')"
          hide-details
        />
        <v-checkbox
          v-model="form.deleteEmptyCollections"
          @change="$v.form.deleteEmptyCollections.$touch()"
          :label="$t('server_settings.label_delete_empty_collections')"
          hide-details
        />
        <v-checkbox
          v-model="form.deleteEmptyReadLists"
          @change="$v.form.deleteEmptyReadLists.$touch()"
          :label="$t('server_settings.label_delete_empty_readlists')"
          hide-details
        />
        <v-text-field
          v-model="form.taskPoolSize"
          @input="$v.form.taskPoolSize.$touch()"
          @blur="$v.form.taskPoolSize.$touch()"
          :error-messages="taskPoolSizeErrors"
          :label="$t('server_settings.label_task_pool_size')"
          type="number"
          min="1"
          class="mt-4"
        />
        <v-text-field
          v-model="form.rememberMeDurationDays"
          @input="$v.form.rememberMeDurationDays.$touch()"
          @blur="$v.form.rememberMeDurationDays.$touch()"
          :error-messages="rememberMeDurationErrors"
          :label="$t('server_settings.label_rememberme_duration')"
          :hint="$t('server_settings.requires_restart')"
          persistent-hint
          type="number"
          min="1"
          class="mt-4"
        />
        <v-checkbox
          v-model="form.renewRememberMeKey"
          @change="$v.form.renewRememberMeKey.$touch()"
          label="Regenerate the RememberMe key"
          persistent-hint
          :hint="$t('server_settings.requires_restart')"
        />

        <v-text-field
          v-model="form.serverPort"
          @input="$v.form.serverPort.$touch()"
          @blur="$v.form.serverPort.$touch()"
          :error-messages="serverPortErrors"
          :placeholder="existingSettings.serverPort?.configurationSource?.toString()"
          :persistent-placeholder="!!existingSettings.serverPort?.configurationSource"
          :hint="$t('server_settings.requires_restart')"
          persistent-hint
          clearable
          :label="$t('server_settings.label_server_port')"
          type="number"
          min="1"
          max="65535"
          class="mt-4"
        >
          <template v-slot:append v-if="!!existingSettings.serverPort?.configurationSource">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on">
                  mdi-information-outline
                </v-icon>
              </template>
              {{ $t('server_settings.config_precedence') }}
            </v-tooltip>
          </template>
        </v-text-field>

        <v-text-field
          v-model="form.serverContextPath"
          @input="$v.form.serverContextPath.$touch()"
          @blur="$v.form.serverContextPath.$touch()"
          :error-messages="serverContextPathErrors"
          :placeholder="existingSettings.serverContextPath?.configurationSource"
          :persistent-placeholder="!!existingSettings.serverContextPath?.configurationSource"
          :hint="$t('server_settings.requires_restart')"
          persistent-hint
          clearable
          :label="$t('server_settings.label_server_context_path')"
          class="mt-4"
        >
          <template v-slot:append v-if="!!existingSettings.serverContextPath?.configurationSource">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on">
                  mdi-information-outline
                </v-icon>
              </template>
              {{ $t('server_settings.config_precedence') }}
            </v-tooltip>
          </template>
        </v-text-field>

        <v-checkbox
          v-model="form.koboProxy"
          @change="$v.form.koboProxy.$touch()"
          :label="$t('server_settings.label_kobo_proxy')"
          hide-details
        />

        <v-text-field
          v-model="form.koboPort"
          @input="$v.form.koboPort.$touch()"
          @blur="$v.form.koboPort.$touch()"
          :error-messages="koboPortErrors"
          clearable
          :label="$t('server_settings.label_kobo_port')"
          :hint="$t('server_settings.hint_kobo_port')"
          persistent-hint
          type="number"
          min="1"
          max="65535"
          class="mt-4"
        />

        <file-browser-dialog
          v-model="modalFileBrowserKepubify"
          @confirm="$v.form.kepubifyPath.$touch()"
          :path.sync="form.kepubifyPath"
          :show-files="true"
        />

        <v-text-field
          v-model="form.kepubifyPath"
          @input="$v.form.kepubifyPath.$touch()"
          @blur="$v.form.kepubifyPath.$touch()"
          :error-messages="serverContextPathErrors"
          :placeholder="existingSettings.kepubifyPath?.configurationSource"
          :persistent-placeholder="!!existingSettings.kepubifyPath?.configurationSource"
          clearable
          :label="$t('server_settings.label_kepubify_path')"
          class="mt-4"
        >
          <template v-slot:append v-if="!!existingSettings.kepubifyPath?.configurationSource">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on">
                  mdi-information-outline
                </v-icon>
              </template>
              {{ $t('server_settings.config_precedence') }}
            </v-tooltip>
          </template>

          <template v-slot:append-outer>
            <v-btn small @click="modalFileBrowserKepubify = true">{{ $t('dialog.edit_library.button_browse') }}</v-btn>
          </template>
        </v-text-field>

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

    <confirmation-dialog
      v-model="dialogRegenerateThumbnails"
      :title="$t('server_settings.dialog_regenerate_thumbnails.title')"
      :body="$t('server_settings.dialog_regenerate_thumbnails.body')"
      :button-confirm="$t('server_settings.dialog_regenerate_thumbnails.btn_confirm')"
      :button-alternate="$t('server_settings.dialog_regenerate_thumbnails.btn_alternate')"
      :button-cancel="$t('server_settings.dialog_regenerate_thumbnails.btn_cancel')"
      @confirm="regenerateThumbnails(true)"
      @alternate="regenerateThumbnails(false)"
    />
  </v-container>
</template>

<script lang="ts">
import {SettingsDto, ThumbnailSizeDto} from '@/types/komga-settings'
import Vue from 'vue'
import {helpers, maxValue, minValue, required} from 'vuelidate/lib/validators'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'

const contextPath = helpers.regex('contextPath', /^\/[-a-zA-Z0-9_\/]*[a-zA-Z0-9]$/)

export default Vue.extend({
  name: 'ServerSettings',
  components: {FileBrowserDialog, ConfirmationDialog},
  data: () => ({
    form: {
      deleteEmptyCollections: false,
      deleteEmptyReadLists: false,
      rememberMeDurationDays: 365,
      renewRememberMeKey: false,
      thumbnailSize: ThumbnailSizeDto.DEFAULT,
      taskPoolSize: 1,
      serverPort: 25600,
      serverContextPath: '',
      koboProxy: false,
      koboPort: undefined,
      kepubifyPath: undefined,
    },
    existingSettings: {} as SettingsDto,
    dialogRegenerateThumbnails: false,
    modalFileBrowserKepubify: false,
  }),
  validations: {
    form: {
      deleteEmptyCollections: {},
      deleteEmptyReadLists: {},
      rememberMeDurationDays: {
        minValue: minValue(1),
        required,
      },
      renewRememberMeKey: {},
      thumbnailSize: {},
      taskPoolSize: {
        minValue: minValue(1),
        required,
      },
      serverPort: {
        minValue: minValue(1),
        maxValue: maxValue(65535),
      },
      serverContextPath: {
        contextPath,
      },
      koboProxy: {},
      koboPort: {
        minValue: minValue(1),
        maxValue: maxValue(65535),
      },
      kepubifyPath: {},
    },
  },
  mounted() {
    this.refreshSettings()
  },
  computed: {
    thumbnailSizes(): any[] {
      return Object.keys(ThumbnailSizeDto).map(x => ({
        text: this.$t(`enums.thumbnail_size.${x}`),
        value: x,
      }))
    },
    rememberMeDurationErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.rememberMeDurationDays?.$dirty) return errors
      !this.$v?.form?.rememberMeDurationDays?.minValue && errors.push(this.$t('validation.one_or_more').toString())
      !this.$v?.form?.rememberMeDurationDays?.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    taskPoolSizeErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.taskPoolSize?.$dirty) return errors
      !this.$v?.form?.taskPoolSize?.minValue && errors.push(this.$t('validation.one_or_more').toString())
      !this.$v?.form?.taskPoolSize?.required && errors.push(this.$t('common.required').toString())
      return errors
    },
    serverPortErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.serverPort?.$dirty) return errors;
      (!this.$v?.form?.serverPort?.minValue || !this.$v?.form?.serverPort?.maxValue) && errors.push(this.$t('validation.tcp_port').toString())
      return errors
    },
    serverContextPathErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.serverContextPath?.$dirty) return errors
      !this.$v?.form?.serverContextPath?.contextPath && errors.push(this.$t('validation.context_path').toString())
      return errors
    },
    koboPortErrors(): string[] {
      const errors = [] as string[]
      if (!this.$v.form?.koboPort?.$dirty) return errors;
      (!this.$v?.form?.koboPort?.minValue || !this.$v?.form?.koboPort?.maxValue) && errors.push(this.$t('validation.tcp_port').toString())
      return errors
    },
    saveDisabled(): boolean {
      return this.$v.form.$invalid || !this.$v.form.$anyDirty
    },
    discardDisabled(): boolean {
      return !this.$v.form.$anyDirty
    },
  },
  methods: {
    async refreshSettings() {
      const settings = await (this.$komgaSettings.getSettings())
      this.$_.merge(this.form, settings)
      this.form.serverPort = settings.serverPort.databaseSource
      this.form.serverContextPath = settings.serverContextPath.databaseSource
      this.form.kepubifyPath = settings.kepubifyPath.databaseSource
      this.$_.merge(this.existingSettings, settings)
      this.$v.form.$reset()
    },
    async saveSettings() {
      const newSettings = {}
      let thumbnailSizeHasChanged = false
      if (this.$v.form?.deleteEmptyCollections?.$dirty)
        this.$_.merge(newSettings, {deleteEmptyCollections: this.form.deleteEmptyCollections})
      if (this.$v.form?.deleteEmptyReadLists?.$dirty)
        this.$_.merge(newSettings, {deleteEmptyReadLists: this.form.deleteEmptyReadLists})
      if (this.$v.form?.rememberMeDurationDays?.$dirty)
        this.$_.merge(newSettings, {rememberMeDurationDays: this.form.rememberMeDurationDays})
      if (this.$v.form?.renewRememberMeKey?.$dirty)
        this.$_.merge(newSettings, {renewRememberMeKey: this.form.renewRememberMeKey})
      if (this.$v.form?.thumbnailSize?.$dirty) {
        this.$_.merge(newSettings, {thumbnailSize: this.form.thumbnailSize})
        thumbnailSizeHasChanged = this.existingSettings.thumbnailSize != this.form.thumbnailSize
      }
      if (this.$v.form?.taskPoolSize?.$dirty)
        this.$_.merge(newSettings, {taskPoolSize: this.form.taskPoolSize})
      if (this.$v.form?.serverPort?.$dirty)
        this.$_.merge(newSettings, {serverPort: this.form.serverPort})
      if (this.$v.form?.serverContextPath?.$dirty)
        // coerce empty string to null
        this.$_.merge(newSettings, {serverContextPath: this.form.serverContextPath || null})

      if (this.$v.form?.koboProxy?.$dirty)
        this.$_.merge(newSettings, {koboProxy: this.form.koboProxy})
      if (this.$v.form?.koboPort?.$dirty)
        this.$_.merge(newSettings, {koboPort: this.form.koboPort})
      if (this.$v.form?.kepubifyPath?.$dirty)
        this.$_.merge(newSettings, {kepubifyPath: this.form.kepubifyPath})


      await this.$komgaSettings.updateSettings(newSettings)
      await this.refreshSettings()

      if (thumbnailSizeHasChanged) {
        this.dialogRegenerateThumbnails = true
      }
    },
    regenerateThumbnails(forBiggerResultOnly: boolean) {
      this.$komgaBooks.regenerateThumbnails(forBiggerResultOnly)
    },
  },
})
</script>

<style scoped>

</style>
