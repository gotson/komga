<template>
  <v-list-item link>
    <v-list-item-content>
      <v-list-item-title
        @click.stop="goto(item)"
        :title="item.title"
        style="width: 100px"
        :class="item.current ? 'primary--text' : ''"
        :style="item.href ? 'cursor: pointer' : undefined"
      >
        <v-badge
          :value="!!item.current"
          color="primary"
          dot
          left
          offset-y="0"
          offset-x="-5"
        />
        {{ item.title }}
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {TocEntry} from '@/types/epub'

export default Vue.extend({
  name: 'TocListItem',
  props: {
    item: {
      type: Object as PropType<TocEntry>,
      required: true,
    },
  },
  methods: {
    goto(element: TocEntry) {
      this.$emit('goto', element)
    },
  },
})
</script>

<style>
</style>
