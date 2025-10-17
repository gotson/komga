<template>
  <v-empty-state
    icon="i-mdi:help-circle"
    color="warning"
    :title="
      $formatMessage({
        description: 'Page not found view: error message title',
        defaultMessage: 'Page not found',
        id: '6WDISV',
      })
    "
    :text="
      $formatMessage({
        description: 'Page not found view: error message subtitle',
        defaultMessage: 'You will be redirected to the home page',
        id: '5b2A0u',
      })
    "
  />
</template>

<script lang="ts" setup>
import { useCountdown } from '@vueuse/core'

const router = useRouter()

useCountdown(5, {
  onComplete() {
    void router.push('/')
  },
}).start()
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
