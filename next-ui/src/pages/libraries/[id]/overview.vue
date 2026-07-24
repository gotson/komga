<template>
  <v-app-bar v-if="display.smAndUp.value">
    <v-spacer />

    <PosterSizeSlider class="me-2" />
  </v-app-bar>

  <div class="pa-4 d-flex flex-column ga-6">
    <OverviewSection
      v-for="section in overviewSections"
      :key="section"
      :section="section"
      :library-id="libraryId"
    />
  </div>
</template>

<script lang="ts" setup>
import { OverviewSectionValues } from '@/types/OverviewSection'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { useDisplay } from 'vuetify/framework'

const display = useDisplay()
const route = useRoute('/libraries/[id]/readlists')
const libraryId = route.params.id

//TODO: get from settings API
const overviewSections = [...OverviewSectionValues]
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
