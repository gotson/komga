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
      <div v-for="(f, i) in filtersOptions"
           :key="i"
      >
        <v-subheader v-if="f.name">{{ f.name }}</v-subheader>
        <v-list-item v-for="v in f.values"
                     :key="v"
                     @click.stop="click(i, v)"
        >
          <v-list-item-icon>
            <v-icon v-if="filtersActive[i].includes(v)" color="secondary">
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

        <v-list-item @click="clearAll()" dense>
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
    // array of object: name, values[]
    filtersOptions: {
      type: Array,
      required: true,
    },
    // array of arrays containing the selection, use with sync
    filtersActive: {
      type: Array,
      required: true,
    },
  },
  computed: {
    filterCustom (): boolean {
      let r = false
      this.filtersActive.forEach(x => {
        if (!this.$_.isEmpty(x)) r = true
      })
      return r
    },
  },
  methods: {
    clearAll () {
      let r = [] as any[]
      this.$_.times(this.filtersActive.length, x => {
        r.push([])
      })

      this.$emit('update:filtersActive', r)
    },
    click (index: number, value: string) {
      let r = this.$_.cloneDeep(this.filtersActive) as any[]
      if (r[index].includes(value)) this.$_.pull(r[index], (value))
      else r[index].push(value)

      this.$emit('update:filtersActive', r)
    },
  },
})
</script>

<style scoped>

</style>
