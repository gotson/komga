<template>
  <v-dialog v-model="modalFileBrowser"
            max-width="450"
            scrollable
  >
    <v-card>

      <v-card-title>{{ dialogTitle }}</v-card-title>

      <v-card-text style="height: 450px">
        <v-text-field
          v-model="selectedPath"
          readonly
        />

        <v-list elevation="3" dense>

          <template v-if="directoryListing.hasOwnProperty('parent')">
            <v-list-item
              @click.prevent="selectParent(directoryListing.parent)"
            >
              <v-list-item-icon>
                <v-icon>mdi-arrow-left</v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>{{ $t('dialog.file_browser.parent_directory') }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-divider/>
          </template>

          <div v-for="(d, index) in directoryListing.directories" :key="index">
            <v-list-item
              @click.prevent="select(d)"
            >
              <v-list-item-icon>
                <v-icon>{{ d.type === 'directory' ? 'mdi-folder' : 'mdi-file' }}</v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>
                  {{ d.name }}
                </v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-divider v-if="index !== directoryListing.directories.length-1"/>
          </div>

          <div v-for="(d, index) in directoryListing.files" :key="index">
            <v-list-item
              @click.prevent="select(d)"
            >
              <v-list-item-icon>
                <v-icon>{{ d.type === 'directory' ? 'mdi-folder' : 'mdi-file' }}</v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>
                  {{ d.name }}
                </v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-divider v-if="index !== directoryListing.files.length-1"/>
          </div>
        </v-list>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ $t('dialog.file_browser.button_cancel') }}</v-btn>
        <v-btn color="primary"
               @click="dialogConfirm"
               :disabled="!selectedPath"
        >{{ confirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {ERROR} from '@/types/events'

export default Vue.extend({
  name: 'FileBrowserDialog',
  data: () => {
    return {
      directoryListing: {} as DirectoryListingDto,
      selectedPath: '',
      modalFileBrowser: false,
    }
  },
  watch: {
    value(val) {
      if (val) this.dialogInit()
      this.modalFileBrowser = val
    },
    modalFileBrowser(val) {
      !val && this.dialogCancel()
    },
  },
  props: {
    value: Boolean,
    path: {
      type: String,
      required: false,
    },
    showFiles: {
      type: Boolean,
      default: false,
    },
    dialogTitle: {
      type: String,
      default: function (): string {
        return this.$t('dialog.file_browser.dialog_title_default').toString()
      },
    },
    confirmText: {
      type: String,
      default: function (): string {
        return this.$t('dialog.file_browser.button_confirm_default').toString()
      },
    },
  },
  methods: {
    dialogInit() {
      try {
        this.getDirs(this.path)
        this.selectedPath = this.path
      } catch (e) {
        this.getDirs()
      }
    },
    dialogCancel() {
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.$emit('input', false)
      this.$emit('update:path', this.selectedPath)
      this.$emit('confirm')
    },
    async getDirs(path?: string) {
      try {
        this.directoryListing = await this.$komgaFileSystem.getDirectoryListing(path, this.showFiles)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    selectParent(path: string) {
      this.selectedPath = path
      this.getDirs(path)
    },
    select(path: PathDto) {
      this.selectedPath = path.path
      if(path.type == 'directory') this.getDirs(path.path)
    },
  },
})
</script>

<style scoped>

</style>
