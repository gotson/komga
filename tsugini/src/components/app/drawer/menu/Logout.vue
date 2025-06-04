<template>
  <q-item
    clickable
    @click="performLogout"
  >
    <q-item-section avatar>
      <q-icon name="mdi-power" />
    </q-item-section>
    <q-item-section>{{
      $formatMessage({
        description: 'Drawer menu for Logout',
        defaultMessage: 'Logout',
        id: 'ti4Pzo',
      })
    }}</q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { useLogout } from 'colada/mutations/logout'

const router = useRouter()
const { mutateAsync: logoutAsync } = useLogout()

function performLogout() {
  void logoutAsync().then(() => router.push('/login'))
}
</script>
