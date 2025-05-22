<template>
  <v-checkbox
    v-for="role in userRoles"
    :key="role.value"
    v-model="user.roles"
    :label="role.text"
    :value="role.value"
    hide-details
  />
</template>

<script setup lang="ts">
import {UserRoles} from '@/types/UserRoles.ts'
import type {components} from '@/generated/openapi/komga'

const user = defineModel<components["schemas"]["UserDto"]>({required: true})

const userRoles = computed(() => Object.keys(UserRoles).map(x => ({
  text: x,
  value: x,
})))
</script>
