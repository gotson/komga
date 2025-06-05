<template>
  <v-divider />
  <div class="d-flex align-center text-caption text-medium-emphasis pa-2">
    <div class="d-flex ms-auto">
      <v-btn
        :prepend-icon="mdiHelpCircleOutline"
        variant="text"
        color="grey"
        size="small"
        class="text-caption"
        href="https://komga.org"
        target="_blank"
        :text="
          $formatMessage({
            description: 'Drawer menu footer: documentation link',
            defaultMessage: 'Documentation',
            id: 'ccAMWS',
          })
        "
      />
    </div>
  </div>
  <div
    v-if="isAdmin"
    class="d-flex align-center text-caption text-medium-emphasis pa-2"
  >
    <div class="d-flex ms-auto">
      <BuildCommit class="me-2" />
      <BuildVersion />
    </div>
  </div>

  <!--  <AppFooter />-->
</template>

<script setup lang="ts">
import mdiHelpCircleOutline from '~icons/mdi/help-circle-outline'
import { useCurrentUser } from '@/colada/queries/current-user'

const { isAdmin } = useCurrentUser()
</script>
