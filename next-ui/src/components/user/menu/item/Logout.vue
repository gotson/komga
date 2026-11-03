<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'Drawer menu for Logout',
        defaultMessage: 'Logout',
        id: 'ti4Pzo',
      })
    "
    prepend-icon="i-mdi:logout"
    @click="performLogout"
  />
</template>

<script setup lang="ts">
import { useLogout } from '@/colada/users'

const router = useRouter()
const { mutateAsync: logoutAsync } = useLogout()

function performLogout() {
  void logoutAsync().then(() => router.push({ name: '/login', query: { logout: '1' } }))
}
</script>
