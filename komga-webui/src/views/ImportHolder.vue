<template>
  <div>
    <v-tabs grow>
      <v-tab :to="{name: 'import-books'}">{{ $t('common.books') }}</v-tab>
      <v-tab :to="{name: 'import-readlists'}">{{ $t('common.readlists') }}</v-tab>
    </v-tabs>
    <router-view/>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'ImportHolder',
})
</script>
