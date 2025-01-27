<template>
  <v-expansion-panels accordion flat tile hover>
    <v-expansion-panel
      v-for="(f, key) in filtersOptions"
      :key="key"
      :disabled="(f.values && f.values.length === 0) && !f.search"
    >
      <v-expansion-panel-header class="text-uppercase ps-1">
        <v-icon
          color="secondary"
          style="max-width: 24px"
          class="mx-0"
          @click.stop="clickFilterMode(key, false)"
        >{{ groupAllOfActive(key) ? 'mdi-filter-multiple' : '' }}
        </v-icon>
        <v-icon
          color="secondary"
          style="max-width: 24px"
          class="me-2"
          @click.stop="clear(key)"
        >{{ groupActive(key) ? 'mdi-checkbox-marked' : '' }}
        </v-icon>
        {{ f.name }}
      </v-expansion-panel-header>
      <v-expansion-panel-content class="no-padding">
        <search-box-base
          v-if="f.search"
          :search-function="f.search"
          @selected="click(key, $event)"
        >
          <template v-slot:item="data">
            <v-list-item-content class="text-body-2">{{ data.item }}</v-list-item-content>
          </template>
        </search-box-base>

        <div style="position: absolute; right: 0; z-index: 1">
          <v-btn-toggle v-if="f.anyAllSelector || groupAllOfActive(key)" mandatory class="semi-transparent"
                        :value="filtersActiveMode[key]?.allOf">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-btn small icon :value="false" v-on="on" @click.stop="clickFilterMode(key, false)">
                  <v-icon small>mdi-filter-outline</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('common.any_of') }}</span>
            </v-tooltip>

            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-btn small icon :value="true" v-on="on" @click.stop="clickFilterMode(key, true)">
                  <v-icon small>mdi-filter-multiple-outline</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('common.all_of') }}</span>
            </v-tooltip>
          </v-btn-toggle>
        </div>

        <v-list
          v-if="f.search || f.values"
          dense
        >
          <!--    Dynamic content from search      -->
          <v-list-item v-for="(v, i) in searchFiltersActive(key)"
                       :key="i"
                       @click.stop="click(key, v)"
          >
            <v-list-item-icon>
              <v-icon color="secondary">mdi-checkbox-marked</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ v }}</v-list-item-title>
          </v-list-item>

          <!--    Static content from filters options      -->
          <v-list-item v-for="v in f.values"
                       :key="JSON.stringify(v.value)"
                       @click.stop="click(key, v.value, v.nValue)"
          >
            <v-list-item-icon>
              <v-icon v-if="key in filtersActive && includes(filtersActive[key], v.nValue)" color="secondary">
                mdi-minus-box
              </v-icon>
              <v-icon v-else-if="key in filtersActive && includes(filtersActive[key], v.value)" color="secondary">
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
import SearchBoxBase from '@/components/SearchBoxBase.vue'
import {FiltersActive, FiltersActiveMode, FiltersOptions} from '@/types/filter'

export default Vue.extend({
  name: 'FilterPanels',
  components: {SearchBoxBase},
  props: {
    filtersOptions: {
      type: Object as PropType<FiltersOptions>,
      required: true,
    },
    filtersActive: {
      type: Object as PropType<FiltersActive>,
      required: true,
    },
    filtersActiveMode: {
      type: Object as PropType<FiltersActiveMode>,
      required: false,
    },
  },
  methods: {
    // filtersActive, filtered to not show options that are in filtersOptions
    searchFiltersActive(key: string): FiltersActive[] {
      if (!(key in this.filtersActive)) return []
      const listedOptions = this.filtersOptions[key]?.values?.flatMap(x => [x.value, x.nValue])
      return this.filtersActive[key].filter((x: string) => !this.$_.includes(listedOptions, x))
    },
    includes(array: any[], value: any): boolean {
      return this.$_.isObject(value) ? this.$_.some(array, value) : this.$_.includes(array, value)
    },
    clear(key: string) {
      let r = this.$_.cloneDeep(this.filtersActive)
      r[key] = []
      if (!this.filtersOptions[key].anyAllSelector) this.clickFilterMode(key, false)

      this.$emit('update:filtersActive', r)
    },
    groupActive(key: string): boolean {
      if (!(key in this.filtersActive)) return false
      return this.filtersActive[key].length > 0
    },
    groupAllOfActive(key: string): boolean {
      if (!this.filtersActiveMode || !(key in this.filtersActiveMode)) return false
      return this.filtersActiveMode[key].allOf
    },
    click(key: string, value: any, nValue?: any) {
      let r = this.$_.cloneDeep(this.filtersActive)
      if (!(key in r)) r[key] = []

      const pull = this.$_.isObject(value) ? this.$_.remove : this.$_.pull
      const includes = this.$_.isObject(value) ? this.$_.some : this.$_.includes

      if (nValue && includes(r[key], nValue))
        pull(r[key], nValue)
      else if (includes(r[key], value)) {
        pull(r[key], value)
        if (nValue) {
          r[key].push(nValue)
          this.clickFilterMode(key, true)
        }
      } else
        r[key].push(value)

      if (!this.filtersOptions[key].anyAllSelector && r[key].length == 0) this.clickFilterMode(key, false)

      this.$emit('update:filtersActive', r)
    },
    clickFilterMode(key: string, value: boolean) {
      if (!this.filtersActiveMode) return
      let r = this.$_.cloneDeep(this.filtersActiveMode)
      r[key] = {allOf: value}

      this.$emit('update:filtersActiveMode', r)
    },
  },
})
</script>

<style>
.no-padding .v-expansion-panel-content__wrap {
  padding: 0;
}

.semi-transparent {
  opacity: 0.5;
}

.semi-transparent:hover {
  opacity: 1;
}
</style>
