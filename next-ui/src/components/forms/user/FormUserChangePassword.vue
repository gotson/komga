<template>
  <v-text-field :rules="rules" />
  <v-text-field />
</template>

<script setup lang="ts">
import type {components} from '@/generated/openapi/komga'

const user = defineModel<components["schemas"]["UserDto"] | undefined>({required: true})

const rules = [
  value => {
    if (value) return true
    return 'You must enter a first name.'
  },
]
</script>
