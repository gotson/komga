<template>
  <div>
    <v-dialog v-model="modal"
              :fullscreen="this.$vuetify.breakpoint.xsOnly"
              :hide-overlay="this.$vuetify.breakpoint.xsOnly"
              max-width="600"
    >
      <form novalidate>
        <v-card>
          <v-toolbar class="hidden-sm-and-up">
            <v-btn icon @click="dialogClose">
              <v-icon>mdi-close</v-icon>
            </v-btn>
            <v-toolbar-title>{{ dialogTitle }}</v-toolbar-title>
            <v-spacer/>
            <v-toolbar-items>
              <v-btn text color="primary" @click="dialogConfirm">{{ confirmText }}</v-btn>
            </v-toolbar-items>
          </v-toolbar>

          <v-card-title class="hidden-xs-only">{{ dialogTitle }}</v-card-title>

          <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-bookshelf</v-icon>
              {{ $t('dialog.edit_library.tab_general') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-tune</v-icon>
              {{ $t('dialog.edit_library.tab_options') }}
            </v-tab>

            <!--  Tab: General  -->
            <v-tab-item>
              <v-card flat>
                <v-container fluid>

                  <v-row>
                    <v-col>
                      <v-text-field v-model="form.name"
                                    autofocus
                                    :label="$t('dialog.edit_library.field_name')"
                                    :error-messages="getErrors('name')"
                                    @input="$v.form.name.$touch()"
                                    @blur="$v.form.name.$touch()"
                      />
                    </v-col>
                  </v-row>

                  <v-row>
                    <v-col cols="8">
                      <file-browser-dialog
                        v-model="modalFileBrowser"
                        :path.sync="form.path"
                        :confirm-text="$t('dialog.edit_library.file_browser_dialog_button_confirm')"
                        :dialog-title="$t('dialog.edit_library.file_browser_dialog_title')"
                      />

                      <v-text-field v-model="form.path"
                                    :label="$t('dialog.edit_library.field_root_folder')"
                                    :error-messages="getErrors('path')"
                                    @input="$v.form.path.$touch()"
                                    @blur="$v.form.path.$touch()"
                      />
                    </v-col>
                    <v-col cols="4">
                      <v-btn @click="modalFileBrowser = true">{{ $t('dialog.edit_library.button_browse') }}</v-btn>
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Options  -->
            <v-tab-item>
              <v-card flat>
                <v-container fluid>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="importComicInfo"
                        :indeterminate="importComicInfo === 1"
                        hide-details
                      >
                        <template v-slot:label>
                          <span class="text-subtitle-2 text--primary">{{ $t('dialog.edit_library.label_import_comicinfo') }}</span>
                        </template>
                      </v-checkbox>
                      <v-checkbox
                        v-model="form.importComicInfoBook"
                        :label="$t('dialog.edit_library.field_import_comicinfo_book')"
                        hide-details
                        class="mx-4"
                      />
                      <v-checkbox
                        v-model="form.importComicInfoSeries"
                        :label="$t('dialog.edit_library.field_import_comicinfo_series')"
                        hide-details
                        class="mx-4"
                      />
                      <v-checkbox
                        v-model="form.importComicInfoCollection"
                        :label="$t('dialog.edit_library.field_import_comicinfo_collections')"
                        hide-details
                        class="mx-4"
                      />
                      <v-checkbox
                        v-model="form.importComicInfoReadList"
                        :label="$t('dialog.edit_library.field_import_comicinfo_readlists')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="importEpub"
                        :indeterminate="importEpub === 1"
                        hide-details
                      >
                        <template v-slot:label>
                          <span class="text-subtitle-2 text--primary">{{ $t('dialog.edit_library.label_import_epub') }}</span>
                        </template>
                      </v-checkbox>
                      <v-checkbox
                        v-model="form.importEpubBook"
                        :label="$t('dialog.edit_library.field_import_epub_book')"
                        hide-details
                        class="mx-4"
                      />
                      <v-checkbox
                        v-model="form.importEpubSeries"
                        :label="$t('dialog.edit_library.field_import_epub_series')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-2">{{ $t('dialog.edit_library.label_import_local') }}</span>
                      <v-checkbox
                        v-model="form.importLocalArtwork"
                        :label="$t('dialog.edit_library.field_import_local_artwork')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-2">{{ $t('dialog.edit_library.label_import_barcode_isbn') }}</span>
                      <v-checkbox
                        v-model="form.importBarcodeIsbn"
                        :label="$t('dialog.edit_library.field_import_barcode_isbn')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col cols="auto">
                      <v-checkbox
                        v-model="scanner"
                        :indeterminate="scanner === 1"
                        hide-details
                      >
                        <template v-slot:label>
                          <span class="text-subtitle-2 text--primary">{{ $t('dialog.edit_library.label_scanner') }}</span>
                        </template>
                      </v-checkbox>
                      <v-checkbox
                        v-model="form.scanForceModifiedTime"
                        :label="$t('dialog.edit_library.field_scanner_force_directory_modified_time')"
                        hide-details
                        class="mx-4"
                      />
                      <v-checkbox
                        v-model="form.scanDeep"
                        :label="$t('dialog.edit_library.field_scanner_deep_scan')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="fileManagement"
                        :indeterminate="fileManagement === 1"
                        hide-details
                      >
                        <template v-slot:label>
                          <span class="text-subtitle-2">{{ $t('dialog.edit_library.label_file_management') }}</span>
                        </template>
                      </v-checkbox>

                      <v-alert type="warning" text class="text-subtitle-2 mt-4">{{ $t('dialog.edit_library.warning_early_feature_repair_extensions') }}</v-alert>
                      <v-checkbox
                        v-model="form.repairExtensions"
                        :label="$t('dialog.edit_library.field_repair_extensions')"
                        hide-details
                        class="mx-4"
                      />

                      <v-alert type="warning" text class="text-subtitle-2 mt-4">{{ $t('dialog.edit_library.warning_early_feature_convert_to_cbz') }}</v-alert>
                      <v-checkbox
                        v-model="form.convertToCbz"
                        :label="$t('dialog.edit_library.field_convert_to_cbz')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>

                </v-container>
              </v-card>
            </v-tab-item>
          </v-tabs>

          <v-card-actions class="hidden-xs-only">
            <v-spacer/>
            <v-btn text @click="dialogClose">{{ $t('dialog.edit_library.button_cancel') }}</v-btn>
            <v-btn text class="primary--text" @click="dialogConfirm">{{ confirmText }}</v-btn>
          </v-card-actions>
        </v-card>
      </form>
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
      >{{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'
import Vue from 'vue'
import {required} from 'vuelidate/lib/validators'

export default Vue.extend({
  name: 'LibraryEditDialog',
  components: {FileBrowserDialog},
  data: () => {
    return {
      modal: false,
      modalFileBrowser: false,
      snackbar: false,
      snackText: '',
      tab: 0,
      form: {
        name: '',
        path: '',
        importComicInfoBook: true,
        importComicInfoSeries: true,
        importComicInfoCollection: true,
        importComicInfoReadList: true,
        importEpubBook: true,
        importEpubSeries: true,
        importLocalArtwork: true,
        importBarcodeIsbn: true,
        scanForceModifiedTime: false,
        scanDeep: false,
        repairExtensions: false,
        convertToCbz: false,
      },
      validationFieldNames: new Map([]),
    }
  },
  computed: {
    dialogTitle(): string {
      return this.library ? this.$t('dialog.edit_library.dialot_title_edit').toString() : this.$t('dialog.edit_library.dialog_title_add').toString()
    },
    confirmText(): string {
      return this.library ? this.$t('dialog.edit_library.button_confirm_edit').toString() : this.$t('dialog.edit_library.button_confirm_add').toString()
    },

    importComicInfo: {
      get: function (): number {
        const val = [this.form.importComicInfoBook, this.form.importComicInfoCollection, this.form.importComicInfoReadList, this.form.importComicInfoSeries]
        const count = val.filter(Boolean).length
        if (count === val.length) return 2
        if (count === 0) return 0
        return 1
      },
      set: function (value: boolean): void {
        this.form.importComicInfoBook = value
        this.form.importComicInfoCollection = value
        this.form.importComicInfoReadList = value
        this.form.importComicInfoSeries = value
      },
    },

    importEpub: {
      get: function (): number {
        const val = [this.form.importEpubBook, this.form.importEpubSeries]
        const count = val.filter(Boolean).length
        if (count === val.length) return 2
        if (count === 0) return 0
        return 1
      },
      set: function (value: boolean): void {
        this.form.importEpubBook = value
        this.form.importEpubSeries = value
      },
    },

    scanner: {
      get: function (): number {
        const val = [this.form.scanDeep, this.form.scanForceModifiedTime]
        const count = val.filter(Boolean).length
        if (count === val.length) return 2
        if (count === 0) return 0
        return 1
      },
      set: function (value: boolean): void {
        this.form.scanDeep = value
        this.form.scanForceModifiedTime = value
      },
    },

    fileManagement: {
      get: function (): number {
        const val = [this.form.repairExtensions, this.form.convertToCbz]
        const count = val.filter(Boolean).length
        if (count === val.length) return 2
        if (count === 0) return 0
        return 1
      },
      set: function (value: boolean): void {
        this.form.repairExtensions = value
        this.form.convertToCbz = value
      },
    },
  },
  props: {
    value: Boolean,
    library: {
      type: Object as () => LibraryDto,
      required: false,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      if (val) this.dialogReset(this.library)
      else this.dialogClose()
    },
  },
  validations: {
    form: {
      name: {required},
      path: {required},
    },
  },
  methods: {
    getErrors(fieldName: string) {
      const errors = []

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        const properName = this.validationFieldNames.has(fieldName)
          ? this.validationFieldNames.get(fieldName) : fieldName.charAt(0).toUpperCase() + fieldName.substring(1)
        errors.push(this.$t('common.required').toString())
      }
      return errors
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    dialogClose() {
      this.$emit('input', false)
      this.tab = 0
    },
    dialogConfirm() {
      this.addLibrary()
    },
    dialogReset(library?: LibraryDto) {
      this.form.name = library ? library.name : ''
      this.form.path = library ? library.root : ''
      this.form.importComicInfoBook = library ? library.importComicInfoBook : true
      this.form.importComicInfoSeries = library ? library.importComicInfoSeries : true
      this.form.importComicInfoCollection = library ? library.importComicInfoCollection : true
      this.form.importComicInfoReadList = library ? library.importComicInfoReadList : true
      this.form.importEpubBook = library ? library.importEpubBook : true
      this.form.importEpubSeries = library ? library.importEpubSeries : true
      this.form.importLocalArtwork = library ? library.importLocalArtwork : true
      this.form.importBarcodeIsbn = library ? library.importBarcodeIsbn : true
      this.form.scanForceModifiedTime = library ? library.scanForceModifiedTime : false
      this.form.scanDeep = library ? library.scanDeep : false
      this.form.repairExtensions = library ? library.repairExtensions : false
      this.form.convertToCbz = library ? library.convertToCbz : false
      this.$v.$reset()
    },
    validateLibrary() {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          name: this.form.name,
          root: this.form.path,
          importComicInfoBook: this.form.importComicInfoBook,
          importComicInfoSeries: this.form.importComicInfoSeries,
          importComicInfoCollection: this.form.importComicInfoCollection,
          importComicInfoReadList: this.form.importComicInfoReadList,
          importEpubBook: this.form.importEpubBook,
          importEpubSeries: this.form.importEpubSeries,
          importLocalArtwork: this.form.importLocalArtwork,
          importBarcodeIsbn: this.form.importBarcodeIsbn,
          scanForceModifiedTime: this.form.scanForceModifiedTime,
          scanDeep: this.form.scanDeep,
          repairExtensions: this.form.repairExtensions,
          convertToCbz: this.form.convertToCbz,
        }
      }
      return null
    },
    async addLibrary() {
      const library = this.validateLibrary()
      if (library) {
        try {
          if (this.library) {
            await this.$store.dispatch('updateLibrary', {libraryId: this.library.id, library: library})
          } else {
            await this.$store.dispatch('postLibrary', library)
          }
          this.dialogClose()
        } catch (e) {
          this.showSnack(e.message)
        }
      } else {
        this.tab = 0
      }
    },
  },
})
</script>

<style scoped>

</style>
