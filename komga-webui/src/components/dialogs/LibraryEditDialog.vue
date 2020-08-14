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
              General
            </v-tab>
            <v-tab class="justify-start">
              <v-icon left class="hidden-xs-only">mdi-tune</v-icon>
              Options
            </v-tab>

            <!--  Tab: General  -->
            <v-tab-item>
              <v-card flat>
                <v-container fluid>

                  <v-row>
                    <v-col>
                      <v-text-field v-model="form.name"
                                    label="Name"
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
                        confirm-text="Choose"
                        dialog-title="Library's root folder"
                      />

                      <v-text-field v-model="form.path"
                                    label="Root folder"
                                    :error-messages="getErrors('path')"
                                    @input="$v.form.path.$touch()"
                                    @blur="$v.form.path.$touch()"
                      />
                    </v-col>
                    <v-col cols="4">
                      <v-btn @click="modalFileBrowser = true">Browse</v-btn>
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
                      <span class="text-subtitle-2">Import metadata for CBR/CBZ containing a ComicInfo.xml file</span>
                      <v-checkbox
                        v-model="form.importComicInfoBook"
                        label="Book metadata"
                        hide-details
                      />
                      <v-checkbox
                        v-model="form.importComicInfoSeries"
                        label="Series title"
                        hide-details
                      />
                      <v-checkbox
                        v-model="form.importComicInfoCollection"
                        label="Collections"
                        hide-details
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-2">Import metadata from EPUB files</span>
                      <v-checkbox
                        v-model="form.importEpubBook"
                        label="Book metadata"
                        hide-details
                      />
                      <v-checkbox
                        v-model="form.importEpubSeries"
                        label="Series title"
                        hide-details
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <span class="text-subtitle-2">Import local media assets</span>
                      <v-checkbox
                        v-model="form.importLocalArtwork"
                        label="Local artwork"
                        hide-details
                      />
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col cols="auto">
                      <span class="text-subtitle-2">Scanner</span>
                      <v-checkbox
                        v-model="form.scanForceModifiedTime"
                        label="Force directory modified time"
                        hide-details
                      />
                      <v-checkbox
                        v-model="form.scanDeep"
                        label="Deep scan"
                        hide-details
                      />
                    </v-col>
                  </v-row>
                </v-container>
              </v-card>
            </v-tab-item>
          </v-tabs>

          <v-card-actions class="hidden-xs-only">
            <v-spacer/>
            <v-btn text @click="dialogClose">Cancel</v-btn>
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
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'
import { LIBRARY_ADDED, LIBRARY_CHANGED, libraryToEventLibraryChanged } from '@/types/events'
import Vue from 'vue'
import { required } from 'vuelidate/lib/validators'

export default Vue.extend({
  name: 'LibraryEditDialog',
  components: { FileBrowserDialog },
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
        importEpubBook: true,
        importEpubSeries: true,
        importLocalArtwork: true,
        scanForceModifiedTime: false,
        scanDeep: false,
      },
      validationFieldNames: new Map([]),
    }
  },
  computed: {
    dialogTitle (): string {
      return this.library ? 'Edit Library' : 'Add Library'
    },
    confirmText (): string {
      return this.library ? 'Edit' : 'Add'
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
    value (val) {
      this.modal = val
    },
    modal (val) {
      if (val) this.dialogReset(this.library)
      else this.dialogClose()
    },
  },
  validations: {
    form: {
      name: { required },
      path: { required },
    },
  },
  methods: {
    getErrors (fieldName: string) {
      const errors = []

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        const properName = this.validationFieldNames.has(fieldName)
          ? this.validationFieldNames.get(fieldName) : fieldName.charAt(0).toUpperCase() + fieldName.substring(1)
        errors.push(`${properName} is required.`)
      }
      return errors
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    dialogClose () {
      this.$emit('input', false)
      this.tab = 0
    },
    dialogConfirm () {
      this.addLibrary()
    },
    dialogReset (library?: LibraryDto) {
      this.form.name = library ? library.name : ''
      this.form.path = library ? library.root : ''
      this.form.importComicInfoBook = library ? library.importComicInfoBook : true
      this.form.importComicInfoSeries = library ? library.importComicInfoSeries : true
      this.form.importComicInfoCollection = library ? library.importComicInfoCollection : true
      this.form.importEpubBook = library ? library.importEpubBook : true
      this.form.importEpubSeries = library ? library.importEpubSeries : true
      this.form.importLocalArtwork = library ? library.importLocalArtwork : true
      this.form.scanForceModifiedTime = library ? library.scanForceModifiedTime : false
      this.form.scanDeep = library ? library.scanDeep : false
      this.$v.$reset()
    },
    validateLibrary () {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          name: this.form.name,
          root: this.form.path,
          importComicInfoBook: this.form.importComicInfoBook,
          importComicInfoSeries: this.form.importComicInfoSeries,
          importComicInfoCollection: this.form.importComicInfoCollection,
          importEpubBook: this.form.importEpubBook,
          importEpubSeries: this.form.importEpubSeries,
          importLocalArtwork: this.form.importLocalArtwork,
          scanForceModifiedTime: this.form.scanForceModifiedTime,
          scanDeep: this.form.scanDeep,
        }
      }
      return null
    },
    async addLibrary () {
      const library = this.validateLibrary()
      if (library) {
        try {
          if (this.library) {
            await this.$store.dispatch('updateLibrary', { libraryId: this.library.id, library: library })
            this.$eventHub.$emit(LIBRARY_CHANGED, libraryToEventLibraryChanged(this.library))
          } else {
            await this.$store.dispatch('postLibrary', library)
            this.$eventHub.$emit(LIBRARY_ADDED)
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
