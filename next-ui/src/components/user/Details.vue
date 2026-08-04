<template>
  <v-card :title="user.email">
    <template #prepend>
      <v-avatar
        color="primary"
        size="x-large"
        ><span class="text-headline-medium text-uppercase">{{
          user.email.charAt(0)
        }}</span></v-avatar
      >
    </template>

    <template #text>
      <div class="d-flex ga-2 flex-wrap">
        <v-chip
          v-for="role in user.roles"
          :key="role"
          :text="$formatMessage(userRolesMessages[role as UserRole])"
          size="small"
          rounded
        />
      </div>
    </template>

    <template
      v-if="slots.actions"
      #actions
    >
      <slot name="actions"></slot>
    </template>
  </v-card>
</template>

<script setup lang="ts">
import { type UserRole, userRolesMessages } from '@/types/UserRoles'
import type { UserDto } from '@/generated/openapi'

const slots = useSlots()

const { user } = defineProps<{
  user: UserDto
}>()
</script>
