<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'Drawer menu for Logout',
        defaultMessage: 'Logout',
        id: 'ti4Pzo',
      })
    "
    prepend-icon="i-mdi:power"
    @click="performLogout"
  />
</template>

<script setup lang="ts">
import { useLogout } from '@/colada/mutations/logout'

const router = useRouter()
const { mutateAsync: logoutAsync } = useLogout()

function performLogout() {
  void logoutAsync().then(() => router.push('/login'))
}
</script>
