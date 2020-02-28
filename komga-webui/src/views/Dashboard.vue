<template>
  <div class="ma-3">

    <edit-series-dialog v-model="dialogEditSingle"
                        :series.sync="editSeriesSingle"
    />

    <horizontal-scroller>
      <template v-slot:prepend>
        <div class="title">Recently Added Series</div>
      </template>
      <template v-slot:content>
        <div v-for="(s, i) in newSeries"
             :key="i">
          <v-skeleton-loader v-if="s === null"
                             :loading="s === null"
                             type="card, text"
                             width="150"
                             height="306.14"
                             class="ma-2 card"
          />
          <card-series v-else
                       :series="s"
                       class="ma-2 card"
                       :edit="singleEdit"
          />
        </div>
      </template>
    </horizontal-scroller>

    <br>

    <horizontal-scroller>
      <template v-slot:prepend>
        <div class="title">Recently Updated Series</div>
      </template>
      <template v-slot:content>
        <div v-for="(s, i) in updatedSeries"
             :key="i">
          <v-skeleton-loader v-if="s === null"
                             :loading="s === null"
                             type="card, text"
                             width="150"
                             height="306.14"
                             class="ma-2 card"
          />
          <card-series v-else
                       :series="s"
                       class="ma-2 card"
                       :edit="singleEdit"
          />
        </div>
      </template>
    </horizontal-scroller>

    <br>

    <horizontal-scroller>
      <template v-slot:prepend>
        <div class="title">Recently Added Books</div>
      </template>
      <template v-slot:content>
        <div v-for="(b, i) in books"
             :key="i"
        >
          <v-skeleton-loader v-if="b === null"
                             :loading="b === null"
                             type="card, text"
                             width="150"
                             height="328.13"
                             class="ma-2 card"
          />
          <card-book v-else
                     :book="b"
                     class="ma-2 card"
          />
        </div>
      </template>
    </horizontal-scroller>

  </div>
</template>

<script lang="ts">
import CardBook from '@/components/CardBook.vue'
import CardSeries from '@/components/CardSeries.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import Vue from 'vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'

export default Vue.extend({
  name: 'Dashboard',
  components: { CardSeries, CardBook, HorizontalScroller, EditSeriesDialog },
  data: () => {
    const pageSize = 20
    return {
      newSeries: Array(pageSize).fill(null) as SeriesDto[],
      updatedSeries: Array(pageSize).fill(null) as SeriesDto[],
      books: Array(pageSize).fill(null) as BookDto[],
      pageSize: pageSize,
      editSeriesSingle: {} as SeriesDto,
      dialogEditSingle: false
    }
  },
  mounted () {
    if (this.$store.state.komgaLibraries.libraries.length === 0) {
      this.$router.push({ name: 'welcome' })
    } else {
      this.loadNewSeries()
      this.loadUpdatedSeries()
      this.loadLatestBooks()
    }
  },
  watch: {
    editSeriesSingle (val: SeriesDto) {
      let index = this.newSeries.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.newSeries.splice(index, 1, val)
      }
      index = this.updatedSeries.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.updatedSeries.splice(index, 1, val)
      }
    }
  },
  methods: {
    async loadNewSeries () {
      this.newSeries = (await this.$komgaSeries.getNewSeries()).content
    },
    async loadUpdatedSeries () {
      this.updatedSeries = (await this.$komgaSeries.getUpdatedSeries()).content
    },
    async loadLatestBooks () {
      const pageRequest = {
        size: this.pageSize,
        sort: ['createdDate,desc']
      } as PageRequest

      this.books = (await this.$komgaBooks.getBooks(undefined, pageRequest)).content
    },
    singleEdit (series: SeriesDto) {
      this.editSeriesSingle = series
      this.dialogEditSingle = true
    }
  }
})
</script>

<style scoped>

</style>
