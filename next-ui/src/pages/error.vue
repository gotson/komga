<template>
  <v-empty-state
    icon="i-mdi:connection"
    :title="
      $formatMessage({
        description: 'Server unreachable error view: error message title',
        defaultMessage: 'Server unreachable',
        id: 'n0TnQ2',
      })
    "
    :text="
      $formatMessage({
        description: 'Server unreachable error view: error message subtitle',
        defaultMessage: 'Check that your Komga server is running',
        id: 'PTr5Wc',
      })
    "
    :action-text="
      $formatMessage({
        description: 'Server unreachable error view: button',
        defaultMessage: 'Retry',
        id: '5tbu8Y',
      })
    "
    @click:action="goHome()"
  />
</template>

<script lang="ts" setup>
const router = useRouter()

function goHome() {
  void router.push('/')
}
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
