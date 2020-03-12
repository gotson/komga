<template>
    <span class="badge">
      <slot/>
    </span>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Badge'
})
</script>

<style scoped>
.badge {
  background-color: #E0E0E0;
  border-radius: 4px;
  color: black;
  display: inline-block;
  padding: 1px 8px;
  text-align: center;
  font-size: .8em;
  font-weight: normal;
}
</style>
