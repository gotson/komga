<template>
  <v-list-group value="Media">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        title="Media"
        prepend-icon="mdi-book-cog"
      />
    </template>

    <v-list-item
      to="/media/analysis"
      title="Media Analysis"
    />
    <v-list-item
      to="/media/missing-posters"
      title="Missing Posters"
    />
    <v-list-item
      to="/media/duplicate-files"
      title="Duplicate Files"
    />

    <v-list-group value="Duplicate Pages">
      <template #activator="{ props }">
        <v-list-item
          v-bind="props"
          title="Duplicate Pages"
        />
      </template>

      <v-list-item
        to="/media/duplicate-pages/known"
        title="Known"
      />
      <v-list-item
        to="/media/duplicate-pages/unknown"
        title="Unknown"
      />
    </v-list-group>
  </v-list-group>
</template>
<script setup lang="ts">
</script>
