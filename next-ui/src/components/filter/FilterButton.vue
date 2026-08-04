<template>
  <!-- We use padding end so that the badge is displayed properly, else it goes off screen -->
  <v-badge
    location="top right"
    color="primary"
    bordered
    :content="count"
    :model-value="count > 0"
    class="pe-4"
    offset-x="7"
    offset-y="7"
  >
    <v-icon-btn
      v-tooltip:bottom="
        $formatMessage({
          description: 'Filter button: tooltip',
          defaultMessage: 'Show filters',
          id: 'kFQatO',
        })
      "
      icon="i-mdi:filter-variant"
      @click="emit('click')"
    />
  </v-badge>
</template>

<script setup lang="ts">
const { count } = defineProps<{
  count: number
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
