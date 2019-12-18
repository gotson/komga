<template>
  <div class="ma-3">

    <div class="title">Recently Added Series</div>

    <v-slide-group show-arrows>
      <v-slide-item v-for="(s, i) in newSeries"
                    :key="i"
      >
        <v-skeleton-loader :loading="s === null"
                           type="card, text"
                           width="150"
                           height="306.14"
                           class="ma-2"
        >
          <card-series :series="s"/>
        </v-skeleton-loader>
      </v-slide-item>
    </v-slide-group>

    <br>

    <div class="title">Recently Updated Series</div>

    <v-slide-group show-arrows>
      <v-slide-item v-for="(s, i) in updatedSeries"
                    :key="i"
      >
        <v-skeleton-loader :loading="s === null"
                           type="card, text"
                           width="150"
                           height="306.14"
                           class="ma-2"
        >
          <card-series :series="s"/>
        </v-skeleton-loader>
      </v-slide-item>
    </v-slide-group>

    <br>

    <div class="title">Recently Added Books</div>

    <v-slide-group show-arrows>
      <v-slide-item v-for="(b, i) in books"
                    :key="i"
      >
        <v-skeleton-loader :loading="b === null"
                           type="card, text"
                           width="150"
                           height="328.13"
                           class="ma-2"
        >
          <card-book :book="b"/>
        </v-skeleton-loader>
      </v-slide-item>
    </v-slide-group>

  </div>
</template>

<script lang="ts">
import CardBook from '@/components/CardBook.vue'
import CardSeries from '@/components/CardSeries.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'Dashboard',
  components: { CardSeries, CardBook },
  data: () => {
    return {
      newSeries: Array(20).fill(null) as SeriesDto[],
      updatedSeries: Array(20).fill(null) as SeriesDto[],
      books: Array(20).fill(null) as BookDto[],
      pageSize: 20
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
    }
  }
})
</script>

<style scoped>

</style>
