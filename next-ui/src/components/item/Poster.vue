<template>
  <v-img
    cover
    position="top"
    :src="posterUrl"
    lazy-src="@/assets/cover.svg"
    aspect-ratio="0.7071"
    rounded
    :max-width="posterMaxWidth"
  >
    <template #placeholder>
      <div class="d-flex align-center justify-center fill-height">
        <v-progress-circular
          color="grey"
          indeterminate
        />
      </div>
    </template>

    <!--  This will just show lazy-src without the v-progress  -->
    <template #error></template>

    <!--  Top-right icon  -->
    <div
      v-if="topRightIcon || topRight"
      class="top-0 right-0 position-absolute translucent text-white px-2 py-1 font-weight-bold text-body-small"
      style="border-bottom-left-radius: 4px"
    >
      <v-icon
        v-if="topRightIcon"
        :icon="topRightIcon"
      />
      <template v-else>{{ topRight }}</template>
    </div>

    <!--  Progress bar  -->
    <v-progress-linear
      v-if="progressPercent"
      :model-value="progressPercent"
      color="primary"
      height="10"
      class="position-absolute bottom-0"
      style="top: unset"
    />
  </v-img>
</template>

<script setup lang="ts">
const { posterMaxWidth = 220 } = defineProps<{
  posterMaxWidth?: number
  posterUrl?: string
  /**
   * Number displayed in the top-right corner.
   */
  topRight?: number
  /**
   * Icon displayed in the top-right corner. Takes precedence over `topRight`.
   */
  topRightIcon?: string
  progressPercent?: number
}>()
</script>
