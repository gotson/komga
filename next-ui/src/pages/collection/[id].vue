<template>Collection: {{ collectionId }}</template>

<script lang="ts" setup>
const route = useRoute('/collection/[id]')
const collectionId = computed(() => route.params.id)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
