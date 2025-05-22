<template>
  <v-text-field />
  <v-text-field />
</template>

<script setup lang="ts">
import type {components} from '@/generated/openapi/komga'

const user = defineModel<components["schemas"]["UserDto"] | undefined>({required: true})
</script>
