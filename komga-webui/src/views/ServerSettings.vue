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
import {ThumbnailSizeDto} from '@/types/komga-settings'
import Vue from 'vue'
import {minValue, required} from 'vuelidate/lib/validators'

export default Vue.extend({
  name: 'ServerSettings',
  data: () => ({
    form: {
      deleteEmptyCollections: false,
      deleteEmptyReadLists: false,
      rememberMeDurationDays: 365,
      renewRememberMeKey: false,
      thumbnailSize: ThumbnailSizeDto.DEFAULT,
    },
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
      this.form.deleteEmptyCollections = settings.deleteEmptyCollections
      this.form.deleteEmptyReadLists = settings.deleteEmptyReadLists
      this.form.rememberMeDurationDays = settings.rememberMeDurationDays
      this.form.renewRememberMeKey = false
      this.form.thumbnailSize = settings.thumbnailSize
      this.$v.form.$reset()
    },
    async saveSettings() {
      const newSettings = {}
      if (this.$v.form?.deleteEmptyCollections?.$dirty)
        this.$_.merge(newSettings, {deleteEmptyCollections: this.form.deleteEmptyCollections})
      if (this.$v.form?.deleteEmptyReadLists?.$dirty)
        this.$_.merge(newSettings, {deleteEmptyReadLists: this.form.deleteEmptyReadLists})
      if (this.$v.form?.rememberMeDurationDays?.$dirty)
        this.$_.merge(newSettings, {rememberMeDurationDays: this.form.rememberMeDurationDays})
      if (this.$v.form?.renewRememberMeKey?.$dirty)
        this.$_.merge(newSettings, {renewRememberMeKey: this.form.renewRememberMeKey})
      if (this.$v.form?.thumbnailSize?.$dirty)
        this.$_.merge(newSettings, {thumbnailSize: this.form.thumbnailSize})

      await this.$komgaSettings.updateSettings(newSettings)
      await this.refreshSettings()
    },
  },
})
</script>

<style scoped>

</style>
