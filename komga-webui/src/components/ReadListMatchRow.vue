<template>
  <tr v-if="match">
    <slot/>
    <td>
      <div v-for="s in match.request.series" :key="s">{{ s }}</div>
    </td>
    <td>{{ match.request.number }}</td>

    <!--  Series picker  -->
    <td @click="modalSeriesPicker = true" style="cursor: pointer">
      <template v-if="selectedSeries">{{ selectedSeries.metadata.title }}</template>
      <template v-else>
        <div style="height: 2em" class="missing"></div>
      </template>
      <series-picker-dialog v-model="modalSeriesPicker" :series.sync="selectedSeries"></series-picker-dialog>
    </td>

    <!--  Book picker  -->
    <td @click="selectedSeries ? modalBookPicker = true : undefined" :style="selectedSeries ? 'cursor: pointer': ''">
      <template v-if="selectedBook">
        {{ selectedBook.metadata.number }} - {{ selectedBook.metadata.title }}
      </template>
      <template v-else-if="selectedSeries">
        <div style="height: 2em" class="missing"></div>
      </template>
      <book-picker-dialog
        v-model="modalBookPicker"
        :books="seriesBooks"
        :book.sync="selectedBook"
      ></book-picker-dialog>
    </td>

    <!--  Error  -->
    <td>
      <template v-if="error">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon color="error" v-on="on">mdi-alert-circle</v-icon>
          </template>
          {{ error }}
        </v-tooltip>
      </template>
      <template v-else-if="duplicate">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon color="warning" v-on="on">mdi-alert-circle</v-icon>
          </template>
          {{ $t('readlist_import.row.duplicate_book') }}
        </v-tooltip>
      </template>
    </td>
  </tr>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {SeriesDto} from '@/types/komga-series'
import {BookDto} from '@/types/komga-books'
import SeriesPickerDialog from '@/components/dialogs/SeriesPickerDialog.vue'
import TransientBookDetailsDialog from '@/components/dialogs/TransientBookDetailsDialog.vue'
import TransientBookViewerDialog from '@/components/dialogs/TransientBookViewerDialog.vue'
import FileNameChooserDialog from '@/components/dialogs/FileNameChooserDialog.vue'
import {ReadListRequestBookMatchesDto} from '@/types/komga-readlists'
import BookPickerDialog from '@/components/dialogs/BookPickerDialog.vue'

export default Vue.extend({
  name: 'ReadListMatchRow',
  components: {
    BookPickerDialog,
    SeriesPickerDialog,
  },
  props: {
    match: {
      type: Object as PropType<ReadListRequestBookMatchesDto>,
      required: true,
    },
    duplicate: {
      type: Boolean,
      default: false,
    },
    bookId: {
      type: String,
      required: false,
    },
  },
  watch: {
    match: {
      handler(val) {
        this.processMatch(val)
      },
      immediate: true,
    },
    selectedSeries: {
      handler(val, old) {
        if (!old) {
          this.getSeriesBooks(val)
        } else if (val?.id !== old?.id) {
          this.selectedBook = undefined
          this.getSeriesBooks(val)
        }
      },
      immediate: true,
    },
    selectedBook: {
      handler(val) {
        this.$emit('update:bookId', val?.id)
      },
      immediate: true,
    },
  },
  data: () => ({
    innerSelect: false,
    selectedSeries: undefined as SeriesDto | undefined,
    selectedBook: undefined as BookDto | undefined,
    seriesBooks: [] as BookDto[],
    modalSeriesPicker: false,
    modalBookPicker: false,
  }),
  computed: {
    existingFileNames(): string[] {
      return this.seriesBooks.map(x => x.name)
    },
    error(): string {
      if (!this.selectedSeries) return this.$t('book_import.row.error_choose_series').toString()
      if (!this.selectedBook) return this.$t('readlist_import.row.error_choose_book').toString()
      return ''
    },
  },
  methods: {
    async processMatch(match: ReadListRequestBookMatchesDto) {
      let seriesId: string | undefined
      if (match.matches.length === 1) {
        seriesId = match.matches[0].seriesId
      } else if (match.matches.length > 1) {
        seriesId = match.matches.find((m) => m.bookIds.length > 0)?.seriesId
      }
      if (seriesId) {
        this.selectedSeries = await this.$komgaSeries.getOneSeries(seriesId)
        const bookId = match.matches.find((m) => m.seriesId === seriesId)?.bookIds.find(Boolean)
        if (bookId) {
          this.selectedBook = await this.$komgaBooks.getBook(bookId)
        }
      }
    },
    async getSeriesBooks(series: SeriesDto) {
      if (series) {
        this.seriesBooks = (await this.$komgaSeries.getBooks(series.id, {unpaged: true})).content
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
