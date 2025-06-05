<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        :icon="mdiTranslate"
      />
    </template>

    <v-list
      :selected="[currentLocale]"
      color="primary"
    >
      <v-list-subheader
        :title="
          $formatMessage({
            description: 'Translations pop-up menu header',
            defaultMessage: 'Translations',
            id: 'InW6ko',
          })
        "
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
        href="https://hosted.weblate.org/projects/komga/next-ui/"
        target="_blank"
      >
        <v-list-item-title class="font-weight-bold">
          {{
            $formatMessage({
              description: 'Translations pop-up menu footer',
              defaultMessage: 'Help us translate',
              id: 'FLqm9f',
            })
          }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import mdiTranslate from '~icons/mdi/translate'
import { availableLocales, currentLocale, setLocale } from '@/utils/i18n/locale-helper'

const locales = Object.entries(availableLocales).map(([k, v]) => ({
  title: v,
  value: k,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
