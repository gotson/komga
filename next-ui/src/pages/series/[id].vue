<template>Series: {{ seriesId }}</template>

<script lang="ts" setup>
const route = useRoute('/series/[id]')
const seriesId = computed(() => route.params.id)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
