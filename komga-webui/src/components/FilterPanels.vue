<template>
  <v-expansion-panels accordion multiple flat tile hover>
    <v-expansion-panel
      v-for="(f, key) in filtersOptions"
      :key="key"
    >
      <v-expansion-panel-header>
        <v-icon
          color="secondary"
          style="max-width: 24px"
          class="mr-2"
          @click.stop="clear(key)"
        >{{ groupActive(key) ? 'mdi-checkbox-marked' : '' }}
        </v-icon>
        {{ f.name }}
      </v-expansion-panel-header>
      <v-expansion-panel-content class="no-padding">
        <v-list dense>
          <v-list-item v-for="v in f.values"
                       :key="v"
                       @click.stop="click(key, v)"
          >
            <v-list-item-icon>
              <v-icon v-if="filtersActive[key].includes(v)" color="secondary">
                mdi-checkbox-marked
              </v-icon>
              <v-icon v-else>
                mdi-checkbox-blank-outline
              </v-icon>
            </v-list-item-icon>
            <v-list-item-title class="text-capitalize">
              {{ v.toString().toLowerCase().replace('_', ' ') }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'FilterPanels',
  props: {
    filtersOptions: {
      type: Object as () => FiltersOptions,
      required: true,
    },
    filtersActive: {
      type: Object as () => FiltersActive,
      required: true,
    },
  },
  methods: {
    clear (key: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      r[key] = []

      this.$emit('update:filtersActive', r)
    },
    groupActive (key: string): boolean {
      for (let v of this.filtersOptions[key].values) {
        if (this.filtersActive[key].includes(v)) {
          return true
        }
      }
      return false
    },
    click (key: string, value: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      if (r[key].includes(value)) this.$_.pull(r[key], (value))
      else r[key].push(value)

      this.$emit('update:filtersActive', r)
    },
  },
})
</script>

<style>
.no-padding .v-expansion-panel-content__wrap {
  padding: 0;
}
</style>
