<template>
  <v-alert
    type="warning"
    variant="tonal"
    class="mb-4"
  >
    <div>The API key will be deleted from this server.</div>
    <ul class="ps-8">
      <li>
        Any application or scripts using this API key will no longer be able to access the Komga
        API.
      </li>
      <li>Authentication activity for this API key will be permanently deleted.</li>
    </ul>
    <div class="font-weight-bold mt-4">This action cannot be undone.</div>
  </v-alert>
</template>

<script setup lang="ts"></script>
