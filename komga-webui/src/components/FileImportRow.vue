<template>
  <tr v-if="book">
    <td>
      <slot></slot>
    </td>

    <td>{{ book.name }}</td>

    <!--  Status icon  -->
    <td>
      <template v-if="bookAnalyzed">
        <v-tooltip bottom :disabled="!status.message">
          <template v-slot:activator="{ on }">
            <v-icon :color="status.color" v-on="on">{{ status.icon }}</v-icon>
          </template>
          {{ convertErrorCodes(status.message) }}
        </v-tooltip>
      </template>
      <v-progress-circular
        v-else
        indeterminate
        color="primary"
        :size="20"
        :width="2"
      />
    </td>

    <!--  Series picker  -->
    <td @click="modalSeriesPicker = true" style="cursor: pointer">
      <template v-if="selectedSeries">{{ selectedSeries.title }}</template>
      <template v-else>
        <div style="height: 2em" class="missing"></div>
      </template>
      <series-picker-dialog v-model="modalSeriesPicker" @update:series="pickedSeries" :include-oneshots="true"/>
    </td>

    <!--  Book number chooser  -->
    <td>
      <v-text-field v-if="!selectedSeries?.oneshot"
                    v-model.number="bookNumber"
                    type="number"
                    step="0.1"
                    dense
                    :disabled="!selectedSeries"
      />
      <span v-else>{{ $t('common.oneshot') }}</span>
    </td>

    <!--  Book details  -->
    <td class="px-1">
      <v-btn icon elevation="1" :disabled="!bookAnalyzed" @click="modalBookDetails = true">
        <v-icon v-if="bookToUpgrade">mdi-file-compare</v-icon>
        <v-icon v-else>mdi-book-information-variant</v-icon>
      </v-btn>
      <transient-book-details-dialog
        v-model="modalBookDetails"
        :left-book="bookAnalyzed"
        :right-book="bookToUpgrade"
        :right-pages="bookToUpgradePages"
      />
    </td>

    <!--  Book viewer  -->
    <td class="px-1">
      <v-btn icon
             elevation="1"
             @click="modalViewer = true"
             :disabled="!bookAnalyzed || bookAnalyzed.status !== MediaStatus.READY"
      >
        <v-icon v-if="bookToUpgrade">mdi-compare</v-icon>
        <v-icon v-else>mdi-image</v-icon>
      </v-btn>
      <transient-book-viewer-dialog
        v-model="modalViewer"
        :left-pages="leftPagesWithUrl"
        :right-pages="rightPagesWithUrl"
      />
    </td>

    <!--  Destination name chooser  -->
    <td class="px-1">
      <v-btn icon
             elevation="1"
             @click="modalNameChooser = true"
             :disabled="!bookAnalyzed || bookAnalyzed.status !== MediaStatus.READY"
      >
        <v-icon>mdi-format-color-text</v-icon>
      </v-btn>
      <file-name-chooser-dialog
        v-model="modalNameChooser"
        :books="seriesBooks"
        :name.sync="destinationName"
        :existing="book.name"
      />
    </td>

    <td>
      {{ destinationName }}
    </td>

    <td>
      <template v-if="error">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon color="error" v-on="on">mdi-alert-circle</v-icon>
          </template>
          {{ error }}
        </v-tooltip>
      </template>
      <template v-if="!error && bookToUpgrade">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon color="warning" v-on="on">mdi-comment-alert</v-icon>
          </template>
          {{ $t('book_import.row.warning_upgrade') }}
        </v-tooltip>
      </template>
    </td>
  </tr>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {MediaStatus} from '@/types/enum-books'
import {SeriesDto} from '@/types/komga-series'
import {TransientBookDto} from '@/types/komga-transientbooks'
import {BookDto, BookImportDto, PageDto, PageDtoWithUrl} from '@/types/komga-books'
import SeriesPickerDialog from '@/components/dialogs/SeriesPickerDialog.vue'
import TransientBookDetailsDialog from '@/components/dialogs/TransientBookDetailsDialog.vue'
import TransientBookViewerDialog from '@/components/dialogs/TransientBookViewerDialog.vue'
import {bookPageUrl, transientBookPageUrl} from '@/functions/urls'
import {convertErrorCodes} from '@/functions/error-codes'
import FileNameChooserDialog from '@/components/dialogs/FileNameChooserDialog.vue'
import {SeriesSelected} from '@/types/series-slim'

export default Vue.extend({
  name: 'FileImportRow',
  components: {SeriesPickerDialog, TransientBookDetailsDialog, TransientBookViewerDialog, FileNameChooserDialog},
  props: {
    book: {
      type: Object as PropType<TransientBookDto>,
      required: true,
    },
    series: {
      type: Object as PropType<SeriesDto>,
      required: false,
    },
    payload: {
      type: Object,
      required: false,
    },
  },
  watch: {
    book: {
      handler(val) {
        this.analyze(val)
        this.destinationName = val.name
      },
      immediate: true,
    },
    selectedSeries: {
      handler(val) {
        this.getSeriesBooks(val)
      },
      immediate: true,
    },
    series: {
      handler(val) {
        if (val) this.selectedSeries = {
          seriesId: val.id,
          title: val.metadata.title,
          oneshot: val.oneshot,
        }
      },
      immediate: true,
    },
    bookNumber: {
      handler(val) {
        this.checkForUpgrade(val)
      },
      immediate: true,
    },
    importPayload: {
      handler(val) {
        this.$emit('update:payload', val)
      },
      immediate: true,
    },
  },
  data: () => ({
    MediaStatus,
    convertErrorCodes,
    innerSelect: false,
    bookAnalyzed: undefined as unknown as TransientBookDto,
    selectedSeries: undefined as SeriesSelected | undefined,
    seriesBooks: [] as BookDto[],
    bookToUpgrade: undefined as BookDto | undefined,
    bookToUpgradePages: [] as PageDto[],
    modalSeriesPicker: false,
    modalBookDetails: false,
    modalViewer: false,
    modalNameChooser: false,
    bookNumber: undefined as number | undefined,
    destinationName: '',
  }),
  computed: {
    leftPagesWithUrl(): PageDtoWithUrl[] {
      return this.bookAnalyzed ? this.bookAnalyzed.pages.map(p => ({
        ...p,
        url: transientBookPageUrl(this.bookAnalyzed.id, p.number),
      })) : []
    },
    rightPagesWithUrl(): PageDtoWithUrl[] {
      return this.bookToUpgrade ? this.bookToUpgradePages.map(p => ({
        ...p,
        url: bookPageUrl(this.bookToUpgrade!!.id, p.number),
      })) : []
    },
    status(): object {
      if (!this.bookAnalyzed) return {}
      switch (this.bookAnalyzed.status) {
        case MediaStatus.READY:
          return {icon: 'mdi-check-circle', color: 'success', message: ''}
        default:
          return {icon: 'mdi-alert-circle', color: 'error', message: this.bookAnalyzed.comment}
      }
    },
    existingFileNames(): string[] {
      return this.seriesBooks.map(x => x.name)
    },
    error(): string {
      if (!this.bookAnalyzed) return this.$t('book_import.row.error_analyze_first').toString()
      if (this.bookAnalyzed.status != MediaStatus.READY) return this.$t('book_import.row.error_only_import_no_errors').toString()
      if (!this.selectedSeries) return this.$t('book_import.row.error_choose_series').toString()
      return ''
    },
    importPayload(): BookImportDto | undefined {
      if (this.error || !this.selectedSeries) return undefined
      return {
        seriesId: this.selectedSeries?.seriesId,
        sourceFile: this.book.url,
        upgradeBookId: this.bookToUpgrade?.id,
        destinationName: this.destinationName,
      }
    },
  },
  methods: {
    async analyze(book: TransientBookDto) {
      this.bookAnalyzed = await this.$komgaTransientBooks.analyze(book.id)
      this.getSeries(this.bookAnalyzed.seriesId)
      this.bookNumber = this.bookAnalyzed.number
    },
    async getSeries(seriesId?: string) {
      if (seriesId) {
        const seriesDto = await this.$komgaSeries.getOneSeries(seriesId)
        this.selectedSeries = {
          seriesId: seriesDto.id,
          title: seriesDto.metadata.title,
          oneshot: seriesDto.oneshot,
        }
      }
    },
    async getSeriesBooks(series: SeriesSelected) {
      if (series) {
        this.seriesBooks = (await this.$komgaSeries.getBooks(series.seriesId, {unpaged: true})).content
        if (series.oneshot) {
          this.bookNumber = this.seriesBooks[0].metadata.numberSort
        }
        this.checkForUpgrade(this.bookNumber)
      }
    },
    async checkForUpgrade(number: number | undefined) {
      this.bookToUpgrade = this.seriesBooks.find(b => b.metadata.numberSort === number)
      if (this.bookToUpgrade) this.bookToUpgradePages = await this.$komgaBooks.getBookPages(this.bookToUpgrade.id)
      else this.bookToUpgradePages = []
    },
    pickedSeries(series: SeriesDto) {
      this.selectedSeries = {
        seriesId: series.id,
        title: series.metadata.title,
        oneshot: series.oneshot,
      }
    },
  },
})
</script>

<style scoped>
.missing {
  border: 2px dashed red;
}
</style>
