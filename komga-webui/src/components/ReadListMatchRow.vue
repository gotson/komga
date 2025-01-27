<template>
  <tr v-if="request">
    <slot/>
    <td>
      <div v-for="s in request.series" :key="s">{{ s }}</div>
    </td>
    <td>{{ request.number }}</td>

    <!--  Series picker  -->
    <td @click="modalSeriesPicker = true" style="cursor: pointer">
      <template v-if="series">
        <div>{{ series.title }}</div>
        <div v-if="series.releaseDate">{{
            new Intl.DateTimeFormat($i18n.locale, {
              year: 'numeric',
              timeZone: 'UTC'
            }).format(new Date(series.releaseDate))
          }}
        </div>
      </template>
      <template v-else>
        <div style="height: 2em" class="missing"></div>
      </template>
      <series-picker-dialog v-model="modalSeriesPicker" @update:series="pickedSeries"></series-picker-dialog>
    </td>

    <!--  Book picker  -->
    <td @click="series ? openBookPicker() : undefined" :style="series ? 'cursor: pointer': ''">
      <template v-if="book">
        {{ book.number }} - {{ book.title }}
      </template>
      <template v-else-if="series">
        <div style="height: 2em" class="missing"></div>
      </template>
      <book-picker-dialog
        v-model="modalBookPicker"
        :books="seriesBooks"
        @update:book="pickedBook"
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
import {
  ReadListRequestBookDto,
  ReadListRequestBookMatchBookDto,
  ReadListRequestBookMatchSeriesDto,
} from '@/types/komga-readlists'
import BookPickerDialog from '@/components/dialogs/BookPickerDialog.vue'
import {BookSearch, SearchConditionSeriesId, SearchOperatorIs} from '@/types/komga-search'

export default Vue.extend({
  name: 'ReadListMatchRow',
  components: {
    BookPickerDialog,
    SeriesPickerDialog,
  },
  props: {
    request: {
      type: Object as PropType<ReadListRequestBookDto>,
      required: true,
    },
    series: {
      type: Object as PropType<ReadListRequestBookMatchSeriesDto>,
      required: false,
    },
    book: {
      type: Object as PropType<ReadListRequestBookMatchBookDto>,
      required: false,
    },
    duplicate: {
      type: Boolean,
      default: false,
    },
    error: {
      type: String,
      default: '',
    },
  },
  watch: {
    series: {
      handler(val: ReadListRequestBookMatchSeriesDto, old: ReadListRequestBookMatchSeriesDto) {
        if (val?.seriesId !== old?.seriesId) {
          this.seriesBooksCached = false
        }
      },
    },
  },
  data: () => ({
    innerSelect: false,
    modalSeriesPicker: false,
    modalBookPicker: false,
    seriesBooks: [] as BookDto[],
    seriesBooksCached: false,
  }),
  computed: {
    existingFileNames(): string[] {
      return this.seriesBooks.map(x => x.name)
    },
  },
  methods: {
    openBookPicker() {
      if (!this.seriesBooksCached) {
        this.$komgaBooks.getBooksList({
          condition: new SearchConditionSeriesId(new SearchOperatorIs(this.series?.seriesId)),
        } as BookSearch, {unpaged: true, sort: 'metadata.numberSort'})
          .then(r => {
            this.seriesBooks = r.content
            this.seriesBooksCached = true
          })
      }
      this.modalBookPicker = true
    },
    pickedSeries(series: SeriesDto) {
      this.$emit('update:series', {
        seriesId: series.id,
        title: series.metadata.title,
      } as ReadListRequestBookMatchSeriesDto)
    },
    pickedBook(book: BookDto) {
      this.$emit('update:book', {
        bookId: book.id,
        number: book.metadata.number,
        title: book.metadata.title,
      } as ReadListRequestBookMatchBookDto)
    },
  },
})
</script>

<style scoped>
.missing {
  border: 2px dashed red;
}
</style>
