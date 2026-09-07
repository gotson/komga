<template>
  <KTooltip location="bottom">
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        :color="isLocked ? 'primary' : 'medium-emphasis'"
        :icon="isLocked ? 'i-mdi:lock' : 'i-mdi:lock-open'"
        variant="text"
        tabindex="-1"
        @click.stop="isLocked = !isLocked"
      />
    </template>
    {{
      isLocked
        ? $formatMessage({
            description: 'Lock icon: tooltip - unlock',
            defaultMessage: 'Unlock field',
            id: 'i6UTPu',
          })
        : $formatMessage({
            description: 'Lock icon: tooltip - lock',
            defaultMessage: 'Lock field',
            id: 'D4qD6S',
          })
    }}
  </KTooltip>
</template>

<script setup lang="ts">
const isLocked = defineModel<boolean>({ required: true })
</script>

<script lang="ts"></script>

<style scoped></style>
