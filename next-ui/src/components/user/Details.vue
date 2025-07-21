<template>
  <v-container class="pa-0">
    <v-row align="center">
      <v-col cols="auto">
        <v-avatar
          color="primary"
          size="x-large"
          ><span class="text-h5 text-uppercase">{{ user.email.charAt(0) }}</span></v-avatar
        >
      </v-col>
      <v-col>
        {{ user.email }}
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <div class="d-flex ga-1 flex-wrap">
          <v-chip
            v-for="role in user.roles"
            :key="role"
            :text="$formatMessage(userRolesMessages[role as UserRoles])"
            size="small"
            rounded
          />
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { UserRoles, userRolesMessages } from '@/types/UserRoles'

const { user } = defineProps<{
  user: components['schemas']['UserDto']
}>()
</script>
