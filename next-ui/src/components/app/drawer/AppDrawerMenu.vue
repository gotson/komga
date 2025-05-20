<template>
  <v-list nav>
    <AppDrawerMenuImport v-if="isAdmin" />
    <AppDrawerMenuMedia v-if="isAdmin" />
    <AppDrawerMenuHistory v-if="isAdmin" />
    <AppDrawerMenuServer v-if="isAdmin" />
    <AppDrawerMenuAccount />
    <AppDrawerMenuLogout />
  </v-list>
</template>

<script setup lang="ts">
import {useCurrentUserRole} from '@/composables/current-user-role.ts'
import {UserRoles} from '@/types/UserRoles.ts'

const {hasRole: isAdmin} = useCurrentUserRole(UserRoles.ADMIN)
</script>
