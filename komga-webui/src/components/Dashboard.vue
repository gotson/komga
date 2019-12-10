<template>
  <div class="ma-3">

    <div class="title">Recently added series</div>

    <v-slide-group show-arrows>
      <v-slide-item v-for="s in series"
                    :key="s.id"
      >
        <card-series :series="s"
                     class="ma-2"
        />
      </v-slide-item>
    </v-slide-group>

    <br>

    <div class="title">Recently added books</div>

    <v-slide-group show-arrows>
      <v-slide-item v-for="b in books"
                    :key="b.id"
      >
        <card-book :book="b"
                   class="ma-2"
        />
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
      series: [] as SeriesDto[],
      books: [] as BookDto[],
      pageSize: 20
    }
  },
  mounted () {
    if (this.$store.state.komgaLibraries.libraries.length === 0) {
      this.$router.push({ name: 'welcome' })
    } else {
      this.loadLatestSeries()
      this.loadLatestBooks()
    }
  },
  methods: {
    async loadLatestSeries () {
      const pageRequest = {
        size: this.pageSize,
        sort: ['createdDate,desc']
      } as PageRequest

      this.series = (await this.$komgaSeries.getSeries(undefined, pageRequest)).content
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
