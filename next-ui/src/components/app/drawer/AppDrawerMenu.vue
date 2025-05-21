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
import {useCurrentUser} from '@/colada/queries/current-user.ts'

const {isAdmin} = useCurrentUser()
</script>
