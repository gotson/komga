<template>
  <v-badge
    location="top right"
    color="primary"
    bordered
    :content="count"
    :model-value="count > 0"
    offset-x="7"
    offset-y="7"
  >
    <v-badge
      location="bottom right"
      color="primary"
      bordered
      dot
      :model-value="dot"
      offset-x="7"
      offset-y="7"
    >
      <v-icon-btn
        v-ktooltip:bottom="
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
  </v-badge>
</template>

<script setup lang="ts">
defineProps<{
  count: number
  dot?: boolean
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
