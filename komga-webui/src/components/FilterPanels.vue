<template>
  <v-expansion-panels accordion flat tile hover>
    <v-expansion-panel
      v-for="(f, key) in filtersOptions"
      :key="key"
      :disabled="f.values.length === 0"
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
                       :key="v.value"
                       @click.stop="click(key, v.value)"
          >
            <v-list-item-icon>
              <v-icon v-if="key in filtersActive && filtersActive[key].includes(v.value)" color="secondary">
                mdi-checkbox-marked
              </v-icon>
              <v-icon v-else>
                mdi-checkbox-blank-outline
              </v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ v.name }}</v-list-item-title>
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
      if (!(key in this.filtersActive)) return false
      for (let v of this.filtersOptions[key].values) {
        if (this.filtersActive[key].includes(v.value)) {
          return true
        }
      }
      return false
    },
    click (key: string, value: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      if (!(key in r)) r[key] = []
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
