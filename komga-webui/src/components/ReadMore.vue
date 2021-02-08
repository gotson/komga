<template>
  <vue-read-more-smooth no-shadow :lines="4">
    <div class="text-body-1"
         style="white-space: pre-wrap"
    >
      <slot/>
    </div>
    <template v-slot:more="value">
      <v-btn text small color="grey darken-1">
        {{ value.open ? $t('read_more.less') : $t('read_more.more') }}
        <v-icon right>mdi-chevron-{{ value.open ? 'up' : 'down' }}</v-icon>
      </v-btn>
    </template>
  </vue-read-more-smooth>
</template>

<script lang="ts">
// @ts-ignore
import VueReadMoreSmooth from "vue-read-more-smooth"
import Vue from 'vue'

export default Vue.extend({
  name: 'ReadMore',
  components: { VueReadMoreSmooth },
})
</script>

<style scoped>

</style>
