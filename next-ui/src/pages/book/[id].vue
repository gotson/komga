<template>Book: {{ bookId }}</template>

<script lang="ts" setup>
const route = useRoute('/book/[id]')
const bookId = computed(() => route.params.id)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
