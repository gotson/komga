<template>
  <div>
    <v-menu offset-y>
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item v-if="!library.unpinned" @click="unpin">
          <v-list-item-title>{{ $t('menu.unpin') }}</v-list-item-title>
        </v-list-item>
        <v-list-item v-if="library.unpinned" @click="pin">
          <v-list-item-title>{{ $t('menu.pin') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="scan(false)" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.scan_library_files') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="scan(true)" class="list-warning" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.scan_library_files_deep') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="confirmAnalyzeModal = true" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.analyze') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="confirmRefreshMetadataModal = true" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.refresh_metadata') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="confirmEmptyTrash = true" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.empty_trash') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="edit" v-if="isAdmin">
          <v-list-item-title>{{ $t('menu.edit') }}</v-list-item-title>
        </v-list-item>
        <v-list-item @click="promptDeleteLibrary"
                     class="list-danger"
                     v-if="isAdmin"
        >
          <v-list-item-title>{{ $t('menu.delete') }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>

    <confirmation-dialog
      v-model="confirmAnalyzeModal"
      :title="$t('dialog.analyze_library.title')"
      :body="$t('dialog.analyze_library.body')"
      :button-confirm="$t('dialog.analyze_library.button_confirm')"
      @confirm="analyze"
    />

    <confirmation-dialog
      v-model="confirmRefreshMetadataModal"
      :title="$t('dialog.refresh_library_metadata.title')"
      :body="$t('dialog.refresh_library_metadata.body')"
      :button-confirm="$t('dialog.refresh_library_metadata.button_confirm')"
      @confirm="refreshMetadata"
    />

    <confirmation-dialog
      v-model="confirmEmptyTrash"
      :title="$t('dialog.empty_trash.title')"
      :body="$t('dialog.empty_trash.body')"
      :button-confirm="$t('dialog.empty_trash.button_confirm')"
      @confirm="emptyTrash"
    />
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import {LibraryDto} from '@/types/komga-libraries'
import {ClientSettingLibraryUpdate} from '@/types/komga-clientsettings'

export default Vue.extend({
  name: 'LibraryActionsMenu',
  components: {ConfirmationDialog},
  props: {
    library: {
      type: Object as () => LibraryDto,
      required: true,
    },
  },
  data: () => {
    return {
      confirmAnalyzeModal: false,
      confirmRefreshMetadataModal: false,
      confirmEmptyTrash: false,
    }
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    unpin() {
      this.$store.dispatch('updateLibrarySetting', {
        libraryId: this.library.id,
        patch: {
          unpinned: true,
        },
      } as ClientSettingLibraryUpdate)
    },
    pin() {
      this.$store.dispatch('updateLibrarySetting', {
        libraryId: this.library.id,
        patch: {
          unpinned: false,
        },
      } as ClientSettingLibraryUpdate)
    },
    scan(scanDeep: boolean) {
      this.$komgaLibraries.scanLibrary(this.library, scanDeep)
    },
    analyze() {
      this.$komgaLibraries.analyzeLibrary(this.library)
    },
    refreshMetadata() {
      this.$komgaLibraries.refreshMetadata(this.library)
    },
    emptyTrash() {
      this.$komgaLibraries.emptyTrash(this.library)
    },
    edit() {
      this.$store.dispatch('dialogEditLibrary', this.library)
    },
    promptDeleteLibrary() {
      this.$store.dispatch('dialogDeleteLibrary', this.library)
    },
  },
})
</script>
<style scoped>
@import "../../styles/list-warning.css";
</style>
