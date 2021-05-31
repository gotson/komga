<template>
  <v-expansion-panels accordion flat tile hover>
    <v-expansion-panel
      v-for="(f, key) in filtersOptions"
      :key="key"
      :disabled="(f.values && f.values.length === 0) && !f.search"
    >
      <v-expansion-panel-header class="text-uppercase">
        <v-icon
          color="secondary"
          style="max-width: 24px"
          class="mx-2"
          @click.stop="clear(key)"
        >{{ groupActive(key) ? 'mdi-checkbox-marked' : '' }}
        </v-icon>
        {{ f.name }}
      </v-expansion-panel-header>
      <v-expansion-panel-content class="no-padding">
        <v-autocomplete
          v-if="f.search"
          v-model="model[key]"
          :items="items[key]"
          :search-input.sync="search[key]"
          :loading="loading[key]"
          :hide-no-data="!search[key] || loading[key]"
          @keydown.esc="search[key] = null"
          multiple
          deletable-chips
          small-chips
          dense
          solo
        />

        <v-list
          v-if="f.values"
          dense
        >
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
import Vue, {PropType} from 'vue'

export default Vue.extend({
  name: 'FilterPanels',
  data: () => {
    return {
      search: {} as any,
      model: {} as any,
      items: {} as any,
      loading: {} as any,
    }
  },
  props: {
    filtersOptions: {
      type: Object as PropType<FiltersOptions>,
      required: true,
    },
    filtersActive: {
      type: Object as PropType<FiltersActive>,
      required: true,
    },
  },
  watch: {
    search: {
      deep: true,
      async handler(val: any) {
        for (const prop in val) {
          if (val[prop] !== null) {
            this.loading[prop] = true
            this.$set(this.items, prop, await (this.filtersOptions[prop] as any).search(val[prop]))
            this.loading[prop] = false
          }
        }
      },
    },
    model: {
      deep: true,
      async handler(val: any) {
        for (const prop in val) {
          if (val[prop] !== null && val[prop] !== this.filtersActive[prop]) {
            let r = this.$_.cloneDeep(this.filtersActive)
            r[prop] = this.$_.clone(val[prop])

            this.$emit('update:filtersActive', r)
          }
        }
      },
    },
    filtersActive: {
      deep: true,
      immediate: true,
      handler(val: any) {
        for (const prop in val) {
          if (val[prop].length > 0) {
            // we need to add existing values to items also, else v-autocomplete won't show it
            this.$set(this.items, prop, this.$_.union(this.items[prop], val[prop]))
            this.$set(this.model, prop, val[prop])
          }
        }
      },
    },
  },
  methods: {
    clear(key: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      r[key] = []

      this.$emit('update:filtersActive', r)
    },
    groupActive(key: string): boolean {
      if (!(key in this.filtersActive)) return false
      return this.filtersActive[key].length > 0;
    },
    click(key: string, value: string) {
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
