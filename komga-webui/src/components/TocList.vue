<template>
  <v-list v-if="toc"
          expand
  >
    <template v-for="(t, i) in toc">
      <v-list-group v-if="t.children"
                    :key="i"
                    no-action
      >
        <template v-slot:activator>
          <toc-list-item :item="t" @goto="goto" class="ps-0"/>
        </template>

        <template v-for="(child, i) in t.children">
          <toc-list-item :key="i"
                         :item="child"
                         @goto="goto"
                         :class="`ms-${(child.level - 1) * 4}`"
          />
        </template>
      </v-list-group>

      <!--  Single item    -->
      <template v-else>
        <toc-list-item :key="i" :item="t" @goto="goto"/>
      </template>
    </template>
  </v-list>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {TocEntry} from '@/types/epub'
import TocListItem from '@/components/TocListItem.vue'

export default Vue.extend({
  name: 'TocList',
  components: {TocListItem},
  data: () => ({}),
  props: {
    toc: {
      type: Array as PropType<TocEntry>[],
      required: false,
    },
  },
  computed: {},
  methods: {
    goto(element: TocEntry) {
      this.$emit('goto', element)
    },
  },
})
</script>

<style>
</style>
