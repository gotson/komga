<template>
  <vue-read-more-smooth no-shadow :lines="4" :open.sync="open">
    <div style="white-space: pre-wrap" class="body-2">
      <slot/>
    </div>
    <template v-slot:more="value">
      <v-btn text small color="grey darken-1">
        {{ value.open ? $t(i18nLess) : $t(i18nMore) }}
        <v-icon right>mdi-chevron-{{ value.open ? 'up' : 'down' }}</v-icon>
      </v-btn>
    </template>
  </vue-read-more-smooth>
</template>

<script lang="ts">
// @ts-ignore
import VueReadMoreSmooth from 'vue-read-more-smooth'
import Vue from 'vue'

export default Vue.extend({
  name: 'ReadMore',
  components: {VueReadMoreSmooth},
  data: () => {
    return {
      open: false,
    }
  },
  watch: {
    value(val) {
      this.open = val
    },
    open(val) {
      this.$emit('input', val)
    },
  },
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    i18nMore: {
      type: String,
      default: 'read_more.more',
    },
    i18nLess: {
      type: String,
      default: 'read_more.less',
    },
  },
})
</script>

<style scoped>

</style>
