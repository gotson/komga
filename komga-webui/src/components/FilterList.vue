<template>
  <v-list dense>
    <div v-for="(f, key) in filtersOptions"
         :key="key"
    >
      <v-subheader v-if="f.name">{{ f.name }}</v-subheader>
      <v-list-item v-for="v in f.values"
                   :key="v.value"
                   @click.stop="click(key, v.value, v.nValue)"
      >
        <v-list-item-icon>
          <v-icon v-if="key in filtersActive && filtersActive[key].includes(v.nValue)" color="secondary">
            mdi-minus-box
          </v-icon>
          <v-icon v-else-if="key in filtersActive && filtersActive[key].includes(v.value)" color="secondary">
            mdi-checkbox-marked
          </v-icon>
          <v-icon v-else>
            mdi-checkbox-blank-outline
          </v-icon>
        </v-list-item-icon>
        <v-list-item-title>{{ v.name }}</v-list-item-title>
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
    click(key: string, value: string, nValue?: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      if (!(key in r)) r[key] = []
      if (nValue && r[key].includes(nValue))
        this.$_.pull(r[key], (nValue))
      else if (r[key].includes(value)) {
        this.$_.pull(r[key], (value))
        if (nValue)
          r[key].push(nValue)
      } else r[key].push(value)

      this.$emit('update:filtersActive', r)
    },
  },
})
</script>

<style scoped>

</style>
