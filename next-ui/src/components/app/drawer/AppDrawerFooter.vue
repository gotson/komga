<template>
  <template v-if="isAdmin">
    <v-divider />
    <div class="d-flex align-center text-caption text-medium-emphasis pa-2">
      <div class="d-flex ms-auto">
        <BuildCommit class="me-2" />
        <BuildVersion />
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import {useCurrentUserRole} from '@/composables/current-user-role.ts'
import {UserRoles} from '@/types/UserRoles.ts'

const {hasRole: isAdmin} = useCurrentUserRole(UserRoles.ADMIN)
</script>
