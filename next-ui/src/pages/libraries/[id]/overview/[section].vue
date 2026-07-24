<template>
  <v-app-bar>
    <v-app-bar-nav-icon
      icon="i-mdi:arrow-left"
      :to="parentRoute"
      exact
    />
    <v-app-bar-title :text="section" />
  </v-app-bar>
  {{ section }}
</template>

<script lang="ts" setup>
import { OverviewSectionsDefault, OverviewSectionValues } from '@/types/OverviewSection'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { useClientSettingsUser, useUpdateClientSettingsUser } from '@/colada/client-settings'
import {
  ClientSettingUser,
  type ClientSettingUserOverviewSection,
} from '@/types/ClientSettingsUser'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { commonMessages } from '@/utils/i18n/common-messages'
import { defineMessage, useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import ReorderOverviewSections from '@/components/ReorderOverviewSections.vue'
import { useMessagesStore } from '@/stores/messages'
import type { RouteLocationRaw } from 'vue-router'

const route = useRoute('/libraries/[id]/overview/[section]')
const libraryViewId = route.params.id
const section = route.params.section

definePage({
  beforeEnter: (to) => {
    const params = to.params as { id: string; section: string }
    const section = params.section

    if (!(OverviewSectionValues as unknown as string[]).includes(section)) {
      return { name: '/libraries/[id]/overview', params: { id: params.id } }
    }
  },
})

const parentRoute = computed<RouteLocationRaw>(() => ({
  name: '/libraries/[id]/overview',
  params: { id: libraryViewId },
}))
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
