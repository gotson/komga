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
<!--          width: 100%; height: 100%; z-index: 1-->
          <div v-for="(v,k) in statusAreas" :key="k">
            <div :style="v.style">
              <slot :name="k"></slot>
            </div>
          </div>

          <v-fade-transition v-if="selection">
            <v-overlay
              v-if="hover || selected || preSelect"
              absolute
              :opacity="hover ? 0.3 : 0"
              :class="`item-border${hover ? '-darken' : ''} overlay-full`"
            >
              <v-icon
                      :color="selected ? 'secondary' : ''"
                      style="position: absolute; top: 5px; left: 10px"
                      @click.stop="selectItem"
              >
                {{ selected || (preSelect && hover) ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline'
                }}
              </v-icon>

              <v-icon v-if="!selected && !preSelect"
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
        >
          <slot name="subtitle">
          </slot>
        </v-card-subtitle>
        <v-card-text class="px-2">
          <slot name="description"></slot>
        </v-card-text>
      </v-card>
    </template>
  </v-hover>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'CardThumbnail',
  props: {
    width: {
      type: [String, Number],
      required: false,
      default: 150
    },
    thumbnail: {
      type: String,
      required: true
    },
    selection: {
      type: Boolean,
      default: false
    },
    selected: {
      type: Boolean,
      default: false
    },
    preSelect: {
      type: Boolean,
      default: false
    }
  },
  data: () => {
    return {
      statusAreas: {
        topleft: {
          style: 'position: absolute; top: 0; left: 0;'
        },
        topright: {
          style: 'position: absolute; top: 0; right: 0;'
        },
        bottom: {
          style: 'position: absolute; bottom: 0; width: 100%;'
        },
        bottomleft: {
          style: 'position: absolute; bottom: 0; left: 0;'
        },
        bottomright: {
          style: 'position: absolute; bottom: 0; right: 0;'
        }
      }
    }
  },
  watch: {
    preSelect (after) {
      this.$emit('update:preSelect', after)
    }
  },
  computed: {
    thumbnailUrl (): string {
      return this.thumbnail
    },
    overlay (): boolean {
      return this.selection
    }
  },
  methods: {
    selectItem () {
      this.$emit('selectItem')
    },
    editItem () {
      this.$emit('editItem')
    },
    goTo () {
      this.$emit('goTo')
    },
    cardClick () {
      if (this.preSelect) {
        this.selectItem()
      } else {
        this.goTo()
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
