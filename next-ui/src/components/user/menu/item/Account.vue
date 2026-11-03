<template>
  <v-list-item :title="currentUser?.email">
    <template #prepend>
      <v-avatar
        color="primary"
        size="32"
      >
        <span class="text-uppercase">{{ currentUser?.email.charAt(0) }}</span>
      </v-avatar>
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import { useCurrentUser } from '@/colada/users'

const { data: currentUser } = useCurrentUser()
</script>
