<template>
  <v-container fluid class="pa-6">
    <v-alert type="info" text class="body-2" dismissible>
      <div>{{ $t('book_import.info_part1') }}</div>
      <div class="mt-2">{{ $t('book_import.info_part2') }}</div>
    </v-alert>

    <v-row align="center">
      <v-col cols="12" sm="">
        <v-text-field
          v-model="importPath"
          clearable
          :label="$t('book_import.field_import_path')"
        />
      </v-col>

      <v-col cols="auto">
        <file-browser-dialog
          v-model="modalFileBrowser"
          :path.sync="importPath"
        />
        <v-btn @click="modalFileBrowser = true">{{ $t('book_import.button_browse') }}</v-btn>
      </v-col>

      <v-col cols="auto">
        <v-btn
          color="primary"
          :disabled="!importPath"
          @click="scanBooks"
        >{{ $t('book_import.button_scan') }}
        </v-btn>
      </v-col>
    </v-row>

    <empty-state
      v-if="scanned && transientBooks.length === 0"
      :title="$t('book_import.no_files_found')"
      :sub-title="$t('book_import.try_another_directory')"
      icon-color="secondary"
      icon="mdi-book-search"
      />

    <template v-if="transientBooks.length > 0">
      <v-divider/>

      <v-simple-table>
        <thead class="font-weight-medium">
        <tr>
          <td>
            <v-checkbox v-model="globalSelect" :indeterminate="globalSelect === 1"></v-checkbox>
          </td>
          <td>{{ $t('book_import.table.file_name') }}</td>
          <td></td>
          <td>{{ $t('book_import.table.series') }}</td>
          <td>{{ $t('book_import.table.number') }}</td>
          <td></td>
          <td></td>
          <td></td>
          <td>{{ $t('book_import.table.destination_name') }}</td>
          <td>
            <v-icon>mdi-alert-circle-outline</v-icon>
          </td>
        </tr>
        </thead>
        <tbody
          v-for="(book, i) in transientBooks"
          :key="i"
        >
        <file-import-row :book="book" :series="selected.includes(i) ? selectedSeries : undefined"
                         :payload.sync="payloads[i]">
          <v-checkbox v-model="selected" :value="i"/>
        </file-import-row>
        </tbody>
      </v-simple-table>

      <v-row align="center">
        <v-col cols="3">
          <v-select v-model="copyMode" :items="copyModes"></v-select>
        </v-col>

        <v-col cols="auto">
          <v-btn @click="modalSeriesPicker = true" :disabled="globalSelect === 0">
            {{ $t('book_import.button_select_series') }}
          </v-btn>
          <series-picker-dialog v-model="modalSeriesPicker" :series.sync="selectedSeries" :include-oneshots="false"/>
        </v-col>
        <v-spacer/>

        <v-col cols="auto">
          <v-btn :color="importFinished ? 'success': 'primary'"
                 :disabled="payloadBatch.books.length === 0"
                 @click="performImport"
          >
            <v-icon left v-if="importFinished">mdi-check</v-icon>
            {{ $t('book_import.button_import') }}
          </v-btn>
        </v-col>
      </v-row>
    </template>

  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import FileBrowserDialog from '@/components/dialogs/FileBrowserDialog.vue'
import FileImportRow from '@/components/FileImportRow.vue'
import {TransientBookDto} from '@/types/komga-transientbooks'
import SeriesPickerDialog from '@/components/dialogs/SeriesPickerDialog.vue'
import {SeriesDto} from '@/types/komga-series'
import {BookImportBatchDto, BookImportDto} from '@/types/komga-books'
import {CopyMode} from '@/types/enum-books'
import {convertErrorCodes} from '@/functions/error-codes'
import {ERROR} from '@/types/events'
import EmptyState from '@/components/EmptyState.vue'

export default Vue.extend({
  name: 'ImportBooks',
  components: {EmptyState, FileBrowserDialog, FileImportRow, SeriesPickerDialog},
  data: () => ({
    modalFileBrowser: false,
    modalSeriesPicker: false,
    selected: [] as number[],
    selectedSeries: undefined as SeriesDto | undefined,
    payloads: [] as BookImportDto[],
    transientBooks: [] as TransientBookDto[],
    scanned: false,
    copyMode: CopyMode.HARDLINK,
    importFinished: false,
  }),
  computed: {
    globalSelect: {
      get: function (): number {
        if (this.selected.length === 0) return 0
        if (this.selected.length === this.transientBooks.length) return 2
        return 1
      },
      set: function (val: boolean): void {
        if (val) this.selected = this.$_.range(this.transientBooks.length)
        else this.selected = []
      },
    },
    importPath: {
      get: function (): string {
        return this.$store.state.persistedState.importPath
      },
      set: function (val: string): void {
        this.$store.commit('setImportPath', val)
      },
    },
    copyModes(): object[] {
      return [
        {text: this.$t('enums.copy_mode.HARDLINK').toString(), value: CopyMode.HARDLINK},
        {text: this.$t('enums.copy_mode.MOVE').toString(), value: CopyMode.MOVE},
      ]
    },
    payloadBatch(): BookImportBatchDto {
      return {
        books: this.selected.map(x => this.payloads[x]).filter(Boolean),
        copyMode: this.copyMode,
      }
    },
  },
  watch: {
    selectedSeries(val) {
      if (val) setTimeout(() => {
        this.selectedSeries = undefined
      }, 100)
    },
  },
  methods: {
    async scanBooks() {
      this.transientBooks = []
      this.scanned = false
      try {
        this.transientBooks = await this.$komgaTransientBooks.scanForTransientBooks(this.importPath)
        this.scanned = true
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: convertErrorCodes(e.message)} as ErrorEvent)
      }
      this.selected = this.$_.range(this.transientBooks.length)
      this.payloads = this.payloads.splice(this.transientBooks.length, this.payloads.length)
      this.importFinished = false
    },
    performImport() {
      if (!this.importFinished) {
        this.$komgaBooks.importBooks(this.payloadBatch)
        this.importFinished = true
      }
    },
  },
})
</script>

<style scoped>

</style>
