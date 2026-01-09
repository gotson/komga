<template>
  <v-card :width="width">
    <v-hover
      v-slot="{ props }"
      v-model="isHovering"
      :disabled="overlayDisabled"
    >
      <div
        class="position-relative bg-scrim"
        v-bind="props"
      >
        <v-img
          id="abc123"
          :contain="!stretchPoster"
          :position="stretchPoster ? 'top' : undefined"
          :src="posterUrl"
          lazy-src="@/assets/cover.svg"
          aspect-ratio="0.7071"
          :class="{ normal: true, inset: selected }"
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

          <div
            v-if="topRightIcon || topRight"
            class="top-0 right-0 position-absolute translucent text-white px-2 py-1 font-weight-bold text-caption"
            style="border-bottom-left-radius: 4px"
          >
            <v-icon
              v-if="topRightIcon"
              :icon="topRightIcon"
            />
            <template v-else>{{ topRight }}</template>
          </div>
        </v-img>

        <!--  The overlay is outside the image, so that we can scale transform the image only -->
        <v-overlay
          :model-value="overlayShown"
          :opacity="overlayTransparent ? 0 : 0.3"
          contained
          transition="fade-transition"
        >
          <v-icon-btn
            :icon="
              preSelect && !isHovering
                ? 'i-mdi:checkbox-blank-circle-outline'
                : 'i-mdi:checkbox-marked-circle'
            "
            :variant="selected || preSelect ? 'text' : 'plain'"
            :color="selected ? 'primary' : 'white'"
            class="top-0 left-0 position-absolute"
            @click="emit('selection', !selected)"
          />
        </v-overlay>
      </div>
    </v-hover>

    <v-card-title class="text-subtitle-2 px-2">{{ title }}</v-card-title>
    <v-card-subtitle
      :class="`px-2 pb-1 ${allowEmptyLine1 ? 'min-height' : undefined} ${line1Classes}`"
      >{{ line1 }}</v-card-subtitle
    >
    <v-card-subtitle
      :class="`px-2 pb-1 ${allowEmptyLine2 ? 'min-height' : undefined} ${line2Classes}`"
      >{{ line2 }}</v-card-subtitle
    >
  </v-card>
</template>

<script setup lang="ts">
import type { ItemCardEmits, ItemCardProps } from '@/types/ItemCard'

const {
  stretchPoster = true,
  width = 150,
  allowEmptyLine1 = false,
  allowEmptyLine2 = false,
  disableSelection = false,
  selected = false,
  preSelect = false,
} = defineProps<
  ItemCardProps & {
    /**
     * Poster URL.
     */
    posterUrl?: string
    /**
     * Whether to stretch the poster or not. If `false`, the image will have the `contain` property.
     *
     * Defaults to `true`.
     */
    stretchPoster?: boolean
    /**
     * Card title. Displayed under the poster.
     */
    title: string
    /**
     * First line of text.
     */
    line1?: string
    /**
     * Classes to apply on `line1`.
     */
    line1Classes?: string
    /**
     * Second line of text.
     */
    line2?: string
    /**
     * Classes to apply on `line2`.
     */
    line2Classes?: string
    /**
     * Whether the `line1` container will be shown even if `line1` is empty.
     *
     * Defaults to `false`.
     */
    allowEmptyLine1?: boolean
    /**
     * Whether the `line2` container will be shown even if `line2` is empty.
     *
     * Defaults to `false`.
     */
    allowEmptyLine2?: boolean

    /**
     * Number displayed in the top-right corner.
     */
    topRight?: number
    /**
     * Icon displayed in the top-right corner. Takes precedence over `topRight`.
     */
    topRightIcon?: string
  }
>()

const emit = defineEmits<ItemCardEmits>()

const isHovering = ref(false)

const overlayDisabled = computed(() => {
  return disableSelection
})
const overlayTransparent = computed(() => (selected || preSelect) && !isHovering.value)
const overlayShown = computed(() => isHovering.value || overlayTransparent.value || preSelect)
</script>

<style lang="scss">
.min-height {
  min-height: 1.5rem;
}

.translucent {
  background-color: rgba(0, 0, 0, 0.7);
}

// Required so that opacity change in VOverlay has transition
.v-overlay__scrim {
  transition: opacity 0.3s ease;
}

// Required as initial state so that transition happens on deselect
.normal {
  transform: scale(1);
  transition: transform 0.135s cubic-bezier(0, 0, 0.2, 1);
}
.inset {
  transform: scale(0.78);
  border-radius: 8px;
}
</style>
