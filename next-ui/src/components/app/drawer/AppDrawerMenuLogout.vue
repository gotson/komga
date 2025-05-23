<template>
  <v-list-item
    title="Logout"
    prepend-icon="mdi-power"
    @click="performLogout"
  />
</template>

<script setup lang="ts">
import {useLogout} from '@/colada/mutations/logout'

const router = useRouter()
const {mutateAsync: logoutAsync} = useLogout()

function performLogout() {
  void logoutAsync().then(() => router.push('/login'))
}
</script>
