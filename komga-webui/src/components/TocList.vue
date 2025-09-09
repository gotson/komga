<template>
  <v-list dense class="toc-list">
    <v-list-item
      v-for="(it, i) in flat"
      :key="i"
      class="toc-row"
      ripple
      @click="onClick(it)"
      @keyup.space.prevent="onKey(it, $event)"
      @keyup.enter.prevent="onKey(it, $event)"
      tabindex="0"
      role="button"
      :aria-label="`Go to ${it.title}`"
    >
      <v-list-item-content>
        <v-list-item-title :style="{ paddingLeft: (it.depth * 16) + 'px' }">
          {{ it.title }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
  </v-list>
</template>

<script lang="ts">
import Vue from 'vue'

type TocEntry = {
  title: string
  href?: string
  page?: number
  children?: TocEntry[]
}

export default Vue.extend({
  name: 'TocList',
  props: {
    toc: { type: Array as () => TocEntry[], required: true },
  },
  computed: {
    flat(): Array<{ title: string; page: number; depth: number; href?: string }> {
      return this.flatten(this.toc || [], 0)
    },
  },
  methods: {
    flatten(nodes: TocEntry[], depth: number): Array<{ title: string; page: number; depth: number; href?: string }> {
      const out: Array<{ title: string; page: number; depth: number; href?: string }> = []
      if (!Array.isArray(nodes)) return out
      for (const n of nodes) {
        out.push({ title: n.title || n.href || 'Untitled', page: Number(n.page || 1), depth, href: n.href })
        if (n.children && n.children.length) {
          out.push(...this.flatten(n.children, depth + 1))
        }
      }
      return out
    },
    onClick(item: { page: number }) {
      // Emit the same payload DivinaReader.goToEntry expects (an object with .page)
      this.$emit('goto', item)
    },
    onKey(item: { page: number }, _e: KeyboardEvent) {
      this.onClick(item)
    },
  },
})
</script>

<style scoped>
.toc-row {
  min-height: 40px;
  cursor: pointer;
  user-select: none;
}
.toc-list {
  /* nicer hitboxes */
}
</style>
