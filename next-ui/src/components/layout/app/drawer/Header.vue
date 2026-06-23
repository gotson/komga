<template>
  <template v-if="display.xs.value">
    <div class="d-flex align-center text-body-medium pa-2">
      <RouterLink to="/">
        <v-avatar start>
          <v-img src="@/assets/logo.svg" />
        </v-avatar>
      </RouterLink>
      Komga
    </div>
    <v-divider />
  </template>
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify'

const display = useDisplay()
</script>
