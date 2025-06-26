<template>
  <v-list nav>
    <LayoutAppDrawerMenuImport v-if="isAdmin" />
    <LayoutAppDrawerMenuMedia v-if="isAdmin" />
    <LayoutAppDrawerMenuHistory v-if="isAdmin" />
    <LayoutAppDrawerMenuServer v-if="isAdmin" />
    <LayoutAppDrawerMenuAccount />
    <LayoutAppDrawerMenuLogout />
  </v-list>
</template>

<script setup lang="ts">
import { useCurrentUser } from '@/colada/queries/current-user'

const { isAdmin } = useCurrentUser()
</script>
