<template>
  <v-dialog v-model="input" scrollable
            @keydown.esc.stop=""
  >
    <v-card :max-height="$vuetify.breakpoint.height * .9" dark>
      <v-card-title>
        <v-pagination
          v-model="page"
          :total-visible="perPage"
          :length="Math.ceil(thumbnails.length/perPage)"
        ></v-pagination>
      </v-card-title>
      <v-card-text>
        <v-container fluid>
          <v-row class="mb-2 align-center justify-space-around">

            <div v-for="(url, i) in visibleThumbnails"
                 :key="url"
                 style="min-height: 220px; max-width: 140px"
                 class="d-flex flex-column justify-center"
            >
              <v-img
                :src="url"
                lazy-src="../../assets/cover.svg"
                aspect-ratio="0.7071"
                :contain="true"
                max-height="200"
                max-width="140"
                class="ma-2"
                @click="input = false; goTo(((page - 1 ) * perPage + i + 1))"
                style="cursor: pointer"
              />
              <div class="white--text text-center font-weight-bold">{{ (page - 1 ) * perPage + i + 1 }}</div>
            </div>

          </v-row>
        </v-container>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import { bookPageThumbnailUrl } from '@/functions/urls'

export default Vue.extend({
  name: 'ThumbnailExplorerDialog',
  props: {
    pagesCount: {
      type: Number,
    },
    value: {
      type: Boolean,
    },
    bookId: {
      type: String,
    },
  },
  data: () => {
    return {
      input: '',
      page: 1,
      perPage: 8,
    }
  },
  watch: {
    value (val) {
      this.input = val
    },
    input (val) {
      !val && this.$emit('input', false)
    },
  },
  computed: {
    thumbnails (): string[] {
      let thumbnails = []
      for (let p = 1; p <= this.pagesCount; p++) {
        thumbnails.push(this.getThumbnailUrl(p))
      }
      return thumbnails
    },
    visibleThumbnails (): String[] {
      let a: number = (this.page - 1) * this.perPage
      let b: number = this.page * this.perPage
      return this.thumbnails.slice(a, b)
    },
  },
  methods: {
    updateInput () {
      this.$emit('input', this.input)
    },
    goTo (page: number) {
      this.$emit('goToPage', page)
    },
    getThumbnailUrl (page: number): string {
      return bookPageThumbnailUrl(this.bookId, page)
    },
  },

})
</script>
