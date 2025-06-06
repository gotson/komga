<template>
  <v-alert
    type="warning"
    variant="tonal"
    class="mb-4"
  >
    <div>The user account will be deleted from this server.</div>
    <ul class="ps-8">
      <li>The read progress for this user account will be permanently deleted.</li>
      <li>Authentication activity for this user will be permanently deleted.</li>
    </ul>
    <div class="font-weight-bold mt-4">This action cannot be undone.</div>
  </v-alert>
</template>

<script setup lang="ts"></script>
