<template>
  <v-hover :disabled="!overlay">
    <template v-slot:default="{ hover }">
      <v-card :width="width"
              :ripple="false"
              @click="cardClick"
      >
        <v-img
          :src="thumbnailUrl"
          lazy-src="../assets/cover.svg"
          aspect-ratio="0.7071"
        >
          <span class="white--text pa-1 px-2 subtitle-2"
                style="background: darkorange; position: absolute; right: 0"
          >
            {{ series.booksCount }}
          </span>
          <v-fade-transition>
            <v-overlay
              v-if="hover || selected || preSelect"
              absolute
              :opacity="hover ? 0.3 : 0"
              :class="`${hover ? 'item-border-darken' : selected ? 'item-border' : 'item-border-transparent'} overlay-full`"
            >
              <v-icon v-if="select"
                      :color="selected ? 'secondary' : ''"
                      style="position: absolute; top: 5px; left: 10px"
                      @click.stop="selectItem"
              >
                {{ selected || (preSelect && hover) ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline'
                }}
              </v-icon>

              <v-icon v-if="!selected && !preSelect && edit"
                      style="position: absolute; bottom: 10px; left: 10px"
                      @click.stop="editItem"
              >
                mdi-pencil
              </v-icon>
            </v-overlay>
          </v-fade-transition>
        </v-img>

        <v-card-subtitle class="pa-2 pb-1 text--primary"
                         v-line-clamp="2"
                         style="word-break: normal !important; height: 4em"
                         :title="series.metadata.title"
        >
          {{ series.metadata.title }}
        </v-card-subtitle>

        <v-card-text class="px-2"
        >
          <span v-if="series.booksCount === 1">{{ series.booksCount }} book</span>
          <span v-else>{{ series.booksCount }} books</span>
        </v-card-text>

      </v-card>
    </template>
  </v-hover>
</template>

<script lang="ts">
import { seriesThumbnailUrl } from '@/functions/urls'
import Vue, { PropType } from 'vue'

export default Vue.extend({
  name: 'CardSeries',
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
  },
  computed: {
    thumbnailUrl (): string {
      return seriesThumbnailUrl(this.series.id)
    },
    overlay (): boolean {
      return this.edit !== undefined || this.select !== undefined
    }
  },
  methods: {
    selectItem () {
      if (this.select !== undefined) {
        this.select()
      }
    },
    editItem () {
      if (this.edit !== undefined) {
        this.edit(this.series)
      }
    },
    cardClick () {
      if (this.preSelect && this.select !== undefined) {
        this.select()
      } else {
        this.$router.push({ name: 'browse-series', params: { seriesId: this.series.id.toString() } })
      }
    }
  }
})
</script>

<style>
.item-border {
  border: 3px solid var(--v-secondary-base);
}

.item-border-transparent {
  border: 3px solid transparent;
}

.item-border-darken {
  border: 3px solid var(--v-secondary-darken2);
}

.overlay-full .v-overlay__content {
  width: 100%;
  height: 100%;
}
</style>
