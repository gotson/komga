<template>
  <v-menu offset-y>
    <template v-slot:activator="{on}">
      <v-btn icon v-on="on">
        <v-icon :color="filterCustom ? 'secondary' : null"
        >mdi-filter-variant
        </v-icon>
      </v-btn>
    </template>
    <v-list>
      <div v-for="(f, key) in filtersOptions"
           :key="key"
      >
        <v-subheader v-if="f.name">{{ f.name }}</v-subheader>
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
      </div>

      <template v-if="filterCustom">
        <v-divider/>

        <v-list-item @click="clearAll" dense>
          <v-list-item-icon>
            <v-icon>mdi-close</v-icon>
          </v-list-item-icon>
          <v-list-item-title>Clear</v-list-item-title>
        </v-list-item>
      </template>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'FilterMenuButton',
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
  computed: {
    filterCustom (): boolean {
      let r = false
      for (const [key, value] of Object.entries(this.filtersActive)) {
        if (!this.$_.isEmpty(value)) r = true
      }
      return r
    },
  },
  methods: {
    clearAll () {
      let r = this.$_.cloneDeep(this.filtersActive)
      for (const key of Object.keys(r)) {
        r[key] = []
      }

      this.$emit('update:filtersActive', r)
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

<style scoped>

</style>
