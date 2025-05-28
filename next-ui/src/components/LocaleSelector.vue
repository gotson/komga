<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        icon="mdi-translate"
      />
    </template>

    <v-list
      :selected="[currentLocale]"
      color="primary"
    >
      <v-list-subheader
        title="Translations"
        class="text-high-emphasis text-uppercase font-weight-black"
      />

      <v-list-item
        v-for="locale in locales"
        :key="locale.value"
        :title="locale.title"
        :value="locale.value"
        @click="setLocale(locale.value)"
      />

      <v-list-item
        href="https://hosted.weblate.org/projects/komga/webui/"
        target="_blank"
      >
        <v-list-item-title class="font-weight-bold">
          Help translate
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import {availableLocales, currentLocale, setLocale} from '@/utils/locale-helper.ts'

const locales = Object.entries(availableLocales).map(([k, v]) => ({
  title: v,
  value: k,
}))
</script>

<script lang="ts">
</script>

<style scoped>

</style>
