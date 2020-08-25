<template>
    <v-list dense>
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
    </v-list>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'FilterList',
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
