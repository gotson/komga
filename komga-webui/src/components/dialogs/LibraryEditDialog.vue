<template>
  <v-dialog v-model="modal"
            :fullscreen="this.$vuetify.breakpoint.xsOnly"
            :hide-overlay="this.$vuetify.breakpoint.xsOnly"
            max-width="700"
            scrollable
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
            <v-btn text color="primary" @click="nextTab" v-if="showNext">
              {{ $t('dialog.edit_library.button_next') }}
            </v-btn>
            <v-btn text color="primary" @click="dialogConfirm" v-else>{{ confirmText }}</v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-card-title class="hidden-xs-only">{{ dialogTitle }}</v-card-title>

        <v-card-text class="pa-0">
          <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-bookshelf</v-icon>
              {{ $t('dialog.edit_library.tab_general') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-magnify-scan</v-icon>
              {{ $t('dialog.edit_library.label_scanner') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-tune</v-icon>
              {{ $t('dialog.edit_library.tab_options') }}
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-book-information-variant</v-icon>
              {{ $t('dialog.edit_library.tab_metadata') }}
            </v-tab>

            <!--  Tab: General  -->
            <v-tab-item>
              <v-card flat :min-height="$vuetify.breakpoint.xs ? $vuetify.breakpoint.height * .8 : undefined">
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

                  <v-row justify="center">
                    <v-col cols="8" align-self="center">
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
                    <v-col cols="4" align-self="center">
                      <v-btn @click="modalFileBrowser = true">{{ $t('dialog.edit_library.button_browse') }}</v-btn>
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Scanner  -->
            <v-tab-item>
              <v-card flat :min-height="$vuetify.breakpoint.xs ? $vuetify.breakpoint.height * .8 : undefined">
                <v-container fluid>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="form.emptyTrashAfterScan"
                        :label="$t('dialog.edit_library.field_scanner_empty_trash_after_scan')"
                        hide-details
                        class="mx-4"
                      />

                      <v-checkbox
                        v-model="form.scanForceModifiedTime"
                        :label="$t('dialog.edit_library.field_scanner_force_directory_modified_time')"
                        hide-details
                        class="mx-4"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="info">mdi-help-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_scanner_force_modified_time') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>

                      <v-checkbox
                        v-model="form.scanOnStartup"
                        :label="$t('dialog.edit_library.field_scanner_scan_startup')"
                        hide-details
                        class="mx-4"
                      />

                      <v-select :items="scanInterval"
                                v-model="form.scanInterval"
                                :label="$t('dialog.edit_library.field_scan_interval')"
                                flat
                                hide-details
                                class="mx-4 mt-3"
                      />

                      <v-text-field v-model="form.oneshotsDirectory"
                                    clearable
                                    :label="$t('dialog.edit_library.field_oneshotsdirectory')"
                                    :error-messages="getErrors('oneshotsDirectory')"
                                    class="mx-4 mt-4"
                      >
                        <template v-slot:append-outer>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="info">mdi-help-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_oneshotsdirectory') }}
                          </v-tooltip>
                        </template>
                      </v-text-field>

                      <div class="mx-4">
                        <span class="text-subtitle-1 text--primary">{{
                            $t('dialog.edit_library.label_scan_types')
                          }}</span>
                        <v-chip-group
                          multiple
                          v-model="form.scanTypes"
                          active-class="primary"
                        >
                          <v-chip v-for="type in fileTypes"
                                  :key="type.value"
                                  :value="type.value"
                                  filter
                                  outlined
                          >{{ type.text }}
                          </v-chip>
                        </v-chip-group>
                      </div>

                      <v-combobox v-model="form.scanDirectoryExclusions"
                                  clearable
                                  multiple
                                  small-chips
                                  deletable-chips
                                  :label="$t('dialog.edit_library.label_scan_directory_exclusions')"
                                  class="mx-4"
                      />
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Options  -->
            <v-tab-item>
              <v-card flat :min-height="$vuetify.breakpoint.xs ? $vuetify.breakpoint.height * .8 : undefined">
                <v-container fluid>
                  <v-row>
                    <v-col cols="auto">
                      <span class="text-subtitle-1 text--primary">{{ $t('dialog.edit_library.label_analysis') }}</span>
                      <v-checkbox
                        v-model="form.hashFiles"
                        :label="$t('dialog.edit_library.field_analysis_hash_files')"
                        hide-details
                        class="mx-4 align-center"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="warning">mdi-alert-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_use_resources') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>

                      <v-checkbox
                        v-model="form.hashPages"
                        :label="$t('dialog.edit_library.field_analysis_hash_pages')"
                        hide-details
                        class="mx-4"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="warning">mdi-alert-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_use_resources') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>

                      <v-checkbox
                        v-model="form.hashKoreader"
                        :label="$t('dialog.edit_library.field_analysis_hash_koreader')"
                        hide-details
                        class="mx-4"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="warning">mdi-alert-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_use_resources') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>

                      <v-checkbox
                        v-model="form.analyzeDimensions"
                        :label="$t('dialog.edit_library.field_analysis_analyze_dimensions')"
                        hide-details
                        class="mx-4"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="warning">mdi-alert-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_use_resources') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="fileManagement"
                        :indeterminate="fileManagement === 1"
                        hide-details
                        :label="$t('dialog.edit_library.label_file_management')"
                      >
                        <template v-slot:label>
                        <span class="text-subtitle-1 text--primary">{{
                            $t('dialog.edit_library.label_file_management')
                          }}</span>
                        </template>
                      </v-checkbox>

                      <v-checkbox
                        v-model="form.repairExtensions"
                        :label="$t('dialog.edit_library.field_repair_extensions')"
                        hide-details
                        class="mx-4"
                      />

                      <v-checkbox
                        v-model="form.convertToCbz"
                        :label="$t('dialog.edit_library.field_convert_to_cbz')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-1 text--primary">{{
                          $t('dialog.edit_library.label_series_cover')
                        }}</span>
                      <v-select :items="seriesCover"
                                v-model="form.seriesCover"
                                :label="$t('dialog.edit_library.field_series_cover')"
                                solo
                                flat
                      />
                    </v-col>
                  </v-row>

                </v-container>
              </v-card>
            </v-tab-item>

            <!--  Tab: Metadata  -->
            <v-tab-item>
              <v-card flat :min-height="$vuetify.breakpoint.xs ? $vuetify.breakpoint.height * .8 : undefined">
                <v-container fluid>
                  <v-row>
                    <v-col>
                      <v-checkbox
                        v-model="importComicInfo"
                        :indeterminate="importComicInfo === 1"
                        hide-details
                      >
                        <template v-slot:label>
                        <span class="text-subtitle-1 text--primary">{{
                            $t('dialog.edit_library.label_import_comicinfo')
                          }}</span>
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
                        v-model="form.importComicInfoSeriesAppendVolume"
                        :label="$t('dialog.edit_library.field_import_comicinfo_series_append_volume')"
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
                        <span class="text-subtitle-1 text--primary">{{
                            $t('dialog.edit_library.label_import_epub')
                          }}</span>
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
                      <span class="text-subtitle-1 text--primary">{{
                          $t('dialog.edit_library.label_import_mylar')
                        }}</span>
                      <v-checkbox
                        v-model="form.importMylarSeries"
                        :label="$t('dialog.edit_library.field_import_mylar_series')"
                        hide-details
                        class="mx-4"
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-1 text--primary">{{
                          $t('dialog.edit_library.label_import_local')
                        }}</span>
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
                      <span class="text-subtitle-1 text--primary">{{
                          $t('dialog.edit_library.label_import_barcode_isbn')
                        }}</span>
                      <v-checkbox
                        v-model="form.importBarcodeIsbn"
                        :label="$t('dialog.edit_library.field_import_barcode_isbn')"
                        hide-details
                        class="mx-4"
                      >
                        <template v-slot:append>
                          <v-tooltip bottom>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" color="warning">mdi-alert-circle-outline</v-icon>
                            </template>
                            {{ $t('dialog.edit_library.tooltip_use_resources') }}
                          </v-tooltip>
                        </template>
                      </v-checkbox>
                    </v-col>
                  </v-row>

                </v-container>
              </v-card>
            </v-tab-item>

          </v-tabs>
        </v-card-text>

        <v-card-actions class="hidden-xs-only">
          <v-spacer/>
          <v-btn text @click="dialogClose">{{ $t('dialog.edit_library.button_cancel') }}</v-btn>
          <v-btn color="primary" @click="nextTab" v-if="showNext">
            {{ $t('dialog.edit_library.button_next') }}
          </v-btn>
          <v-btn color="primary" @click="dialogConfirm" v-else>{{ confirmText }}</v-btn>
        </v-card-actions>
      </v-card>
    </form>
  </v-dialog>
</template>

<script lang="ts">
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'
import Vue from 'vue'
import {required} from 'vuelidate/lib/validators'
import {ERROR} from '@/types/events'
import {ScanIntervalDto, SeriesCoverDto} from '@/types/enum-libraries'
import {LibraryDto} from '@/types/komga-libraries'

export default Vue.extend({
  name: 'LibraryEditDialog',
  components: {FileBrowserDialog},
  data: () => {
    return {
      modal: false,
      modalFileBrowser: false,
      tab: 0,
      form: {
        name: '',
        path: '',
        importComicInfoBook: true,
        importComicInfoSeries: true,
        importComicInfoCollection: true,
        importComicInfoReadList: true,
        importComicInfoSeriesAppendVolume: true,
        importEpubBook: true,
        importEpubSeries: true,
        importMylarSeries: true,
        importLocalArtwork: true,
        importBarcodeIsbn: false,
        scanForceModifiedTime: false,
        scanInterval: ScanIntervalDto.EVERY_6H,
        scanOnStartup: false,
        scanTypes: [],
        scanDirectoryExclusions: [] as string[],
        repairExtensions: false,
        convertToCbz: false,
        emptyTrashAfterScan: false,
        seriesCover: SeriesCoverDto.FIRST as SeriesCoverDto,
        hashFiles: true,
        hashPages: false,
        hashKoreader: false,
        analyzeDimensions: true,
        oneshotsDirectory: '',
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
    showNext(): boolean {
      return !this.library && this.tab !== 3
    },
    seriesCover(): any[] {
      return Object.keys(SeriesCoverDto).map(x => ({
        text: this.$t(`enums.series_cover.${x}`),
        value: x,
      }))
    },
    scanInterval(): any[] {
      return Object.keys(ScanIntervalDto).map(x => ({
        text: this.$t(`enums.scan_interval.${x}`),
        value: x,
      }))
    },
    fileTypes(): any[] {
      return [{
        text: this.$t('common.cbx').toString(),
        value: 'cbx',
      }, {
        text: this.$t('common.pdf').toString(),
        value: 'pdf',
      }, {
        text: this.$t('common.epub').toString(),
        value: 'epub',
      }]
    },

    importComicInfo: {
      get: function (): number {
        const val = [this.form.importComicInfoBook, this.form.importComicInfoCollection, this.form.importComicInfoReadList, this.form.importComicInfoSeries, this.form.importComicInfoSeriesAppendVolume]
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
        this.form.importComicInfoSeriesAppendVolume = value
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
    nextTab() {
      this.$v.$touch()
      if (!this.$v.$invalid) this.tab += 1
    },
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
      this.form.importComicInfoSeriesAppendVolume = library ? library.importComicInfoSeriesAppendVolume : true
      this.form.importEpubBook = library ? library.importEpubBook : true
      this.form.importEpubSeries = library ? library.importEpubSeries : true
      this.form.importMylarSeries = library ? library.importMylarSeries : true
      this.form.importLocalArtwork = library ? library.importLocalArtwork : true
      this.form.importBarcodeIsbn = library ? library.importBarcodeIsbn : false
      this.form.scanForceModifiedTime = library ? library.scanForceModifiedTime : false
      this.form.scanInterval = library ? library.scanInterval : ScanIntervalDto.EVERY_6H
      this.form.scanOnStartup = library ? library.scanOnStartup : false
      this.form.scanTypes = []
      if (!library) this.form.scanTypes = ['cbx', 'pdf', 'epub']
      if (library?.scanEpub == true) this.form.scanTypes.splice(0, 0, 'epub')
      if (library?.scanPdf == true) this.form.scanTypes.splice(0, 0, 'pdf')
      if (library?.scanCbx == true) this.form.scanTypes.splice(0, 0, 'cbx')
      this.form.scanDirectoryExclusions = library ? library.scanDirectoryExclusions : ['#recycle', '@eaDir', '@Recycle']
      this.form.repairExtensions = library ? library.repairExtensions : false
      this.form.convertToCbz = library ? library.convertToCbz : false
      this.form.emptyTrashAfterScan = library ? library.emptyTrashAfterScan : false
      this.form.seriesCover = library ? library.seriesCover : SeriesCoverDto.FIRST
      this.form.hashFiles = library ? library.hashFiles : true
      this.form.hashPages = library ? library.hashPages : false
      this.form.hashKoreader = library ? library.hashKoreader : false
      this.form.analyzeDimensions = library ? library.analyzeDimensions : true
      this.form.oneshotsDirectory = library ? library.oneshotsDirectory : ''
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
          importComicInfoSeriesAppendVolume: this.form.importComicInfoSeriesAppendVolume,
          importEpubBook: this.form.importEpubBook,
          importEpubSeries: this.form.importEpubSeries,
          importMylarSeries: this.form.importMylarSeries,
          importLocalArtwork: this.form.importLocalArtwork,
          importBarcodeIsbn: this.form.importBarcodeIsbn,
          scanForceModifiedTime: this.form.scanForceModifiedTime,
          scanInterval: this.form.scanInterval,
          scanOnStartup: this.form.scanOnStartup,
          scanCbx: this.form.scanTypes.includes('cbx'),
          scanPdf: this.form.scanTypes.includes('pdf'),
          scanEpub: this.form.scanTypes.includes('epub'),
          scanDirectoryExclusions: this.form.scanDirectoryExclusions,
          repairExtensions: this.form.repairExtensions,
          convertToCbz: this.form.convertToCbz,
          emptyTrashAfterScan: this.form.emptyTrashAfterScan,
          seriesCover: this.form.seriesCover,
          hashFiles: this.form.hashFiles,
          hashPages: this.form.hashPages,
          hashKoreader: this.form.hashKoreader,
          analyzeDimensions: this.form.analyzeDimensions,
          oneshotsDirectory: this.form.oneshotsDirectory,
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
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
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
