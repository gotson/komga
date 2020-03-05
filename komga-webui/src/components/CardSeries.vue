<template>
  <card-thumbnail
    :thumbnail="thumbnail"
    :width="width"
    :selection="selection"
    :edit="edit"
    :preSelect="preSelect"
    :select="select"
    :selected="selected"
    @editItem="editItem"
    @selectItem="selectItem"
    @goTo="goTo"
  >

    <template v-slot:topright>
       <span class="white--text pt-2 pb-1 px-2 subtitle-2" style="background: darkorange none repeat scroll 0% 0%;">
         {{ series.booksCount }}
       </span>
    </template>
    <template #subtitle>
      {{ title }}
    </template>
    <template #description>
      <span v-if="series.booksCount === 1">{{ series.booksCount }} book</span>
      <span v-else>{{ series.booksCount }} books</span>
    </template>
  </card-thumbnail>
</template>

<script lang="ts">
import { seriesThumbnailUrl } from '@/functions/urls'
import Vue, { PropType } from 'vue'
import CardThumbnail from '@/components/CardThumbnail.vue'

export default Vue.extend({
  name: 'CardSeries',
  components: { CardThumbnail },
  computed: {
    thumbnail (): string {
      return seriesThumbnailUrl(this.series.id)
    },
    title (): string {
      return this.series.metadata.title
    }
  },
  methods: {
    editItem () {
      if (this.edit) {
        this.edit(this.series)
      }
    },
    selectItem () {
      if (this.select) {
        this.select()
      }
    },
    goTo () {
      this.$router.push({ name: 'browse-series', params: { seriesId: this.series.id.toString() } })
    }
  },
  props: {
    series: {
      type: Object as PropType<SeriesDto>,
      required: true
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150
    },
    selection: {
      type: Boolean,
      default: true
    },
    selected: {
      type: Boolean,
      default: false
    },
    preSelect: {
      type: Boolean,
      default: false
    },
    select: {
      type: Function,
      required: false
    },
    edit: {
      type: Function,
      required: false
    }
  }
})
</script>
