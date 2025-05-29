<template>
  <v-divider />
  <div class="d-flex align-center text-caption text-medium-emphasis pa-2">
    <div class="d-flex ms-auto">
      <v-btn
        prepend-icon="mdi-help-circle-outline"
        variant="text"
        color="grey"
        size="small"
        class="text-caption"
        href="https://komga.org"
        target="_blank"
        text="Documentation"
      />
    </div>
  </div>
  <div
    v-if="isAdmin"
    class="d-flex align-center text-caption text-medium-emphasis pa-2"
  >
    <div class="d-flex ms-auto">
      <BuildCommit class="me-2" />
      <BuildVersion />
    </div>
  </div>
</template>

<script setup lang="ts">
import {useCurrentUser} from '@/colada/queries/current-user.ts'

const {isAdmin} = useCurrentUser()
</script>
