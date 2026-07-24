<template>
  <v-app-bar v-if="display.smAndUp.value">
    <v-spacer />

    <PosterSizeSlider class="me-2" />
  </v-app-bar>

  <div class="pa-4 d-flex flex-column ga-6">
    <OverviewSection
      v-for="section in overviewSections"
      :key="section.section"
      :section="section"
      :library-view-id="libraryViewId"
    />
  </div>
</template>

<script lang="ts" setup>
import { OverviewSectionsDefault } from '@/types/OverviewSection'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { useDisplay } from 'vuetify/framework'
import { useClientSettingsUser } from '@/colada/client-settings'
import { ClientSettingUser } from '@/types/ClientSettingsUser'

const display = useDisplay()
const route = useRoute('/libraries/[id]/readlists')
const libraryViewId = route.params.id

const { userSettings } = useClientSettingsUser()
const overviewSections = computed(() => {
  return (
    userSettings.value[ClientSettingUser.NextUIOverviewSections]?.[libraryViewId] ??
    OverviewSectionsDefault
  )
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
