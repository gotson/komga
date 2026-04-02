<template>Readlist: {{ readListId }}</template>

<script lang="ts" setup>
const route = useRoute('/readlist/[id]')
const readListId = computed(() => route.params.id)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
