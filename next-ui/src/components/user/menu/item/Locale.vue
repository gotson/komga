<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'User menu: language',
        defaultMessage: 'Language',
        id: 'vcA12h',
      })
    "
    :subtitle="currentLocaleTitle"
    prepend-icon="i-mdi:translate"
    :append-icon="isRtl ? 'i-mdi:menu-left' : 'i-mdi:menu-right'"
  >
    <v-menu
      submenu
      open-on-click
      open-on-hover
      activator="parent"
    >
      <LocaleList />
    </v-menu>
  </v-list-item>
</template>

<script setup lang="ts">
import { availableLocales, currentLocale } from '@/utils/i18n/locale-helper'
import { useRtl } from 'vuetify/framework'

const { isRtl } = useRtl()

const currentLocaleTitle = availableLocales[currentLocale]?.nameLocalized
</script>
