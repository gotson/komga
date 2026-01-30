<template>
  <v-hover
    v-slot="{ props }"
    v-model="isHovering"
  >
    <v-card
      v-on-long-press.prevent="onCardLongPress"
      v-bind="props"
      :elevation="isHovering ? 3 : 1"
      :class="isPreSelect || selected ? 'cursor-pointer' : 'cursor-default'"
      @click="
        (event: Event) => (isPreSelect || selected ? emit('selection', !selected, event) : {})
      "
    >
      <v-card-text>
        <div class="d-flex flex-row ga-4">
          <div>
            <v-fade-transition>
              <v-icon-btn
                v-if="showSelection"
                :icon="
                  selected || (isPreSelect && isHovering)
                    ? 'i-mdi:checkbox-marked-circle'
                    : 'i-mdi:checkbox-blank-circle-outline'
                "
                :color="selected ? 'primary' : undefined"
                :variant="selected ? 'text' : 'flat'"
                class="position-absolute top-0 left-0"
                style="z-index: 2"
                @click.stop="(event: Event) => emit('selection', !selected, event)"
              />
            </v-fade-transition>

            <v-img
              :cover="stretchPoster"
              :position="stretchPoster ? 'top ' : undefined"
              :src="posterUrl"
              lazy-src="@/assets/cover.svg"
              aspect-ratio="0.7071"
              :class="{ normal: true, inset: selected }"
              :width="width"
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
                class="top-0 right-0 position-absolute translucent text-white px-2 py-1 font-weight-bold text-caption"
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
          </div>

          <div>
            <div
              class="text-h6 force-line-count text-wrap"
              :style="[{ '--lines': 1 }, { '--line-height': 1.6 }]"
            >
              {{ title }}
            </div>
            <div
              class="text-subtitle-1 force-line-count text-pre-wrap mt-2"
              :style="[{ '--lines': display.xs.value ? 1 : 3 }, { '--line-height': 1.6 }]"
            >
              {{ text }}
            </div>
          </div>

          <!--  icon holder  -->
          <div class="d-flex flex-column ga-1 position-absolute top-0 right-0 mt-2 me-2">
            <!--  menu icon  -->
            <v-fade-transition>
              <v-icon-btn
                v-if="menuIcon && showMenu"
                :icon="menuIcon"
                v-bind="menuProps"
                @click.stop="emit('clickMenu')"
              />
            </v-fade-transition>

            <!--  quick action icon  -->
            <v-fade-transition>
              <v-icon-btn
                v-if="quickActionIcon && showQuickAction"
                :icon="quickActionIcon"
                v-bind="quickActionProps"
                @click.stop="emit('clickQuickAction')"
              />
            </v-fade-transition>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </v-hover>
</template>

<script setup lang="ts">
import type { ItemCardEmits, ItemCardProps } from '@/types/ItemCard'
import { vOnLongPress } from '@vueuse/components'
import { usePrimaryInput } from '@/composables/device'
import { useDisplay } from 'vuetify'

const display = useDisplay()
const { isTouchPrimary } = usePrimaryInput()

function onCardLongPress() {
  if (isTouchPrimary.value) emit('cardLongPress')
}

const {
  stretchPoster,
  width = 150,
  disableSelection = false,
  selected = false,
  preSelect = false,
  quickActionIcon,
  quickActionProps = {},
  menuIcon,
} = defineProps<
  ItemCardProps & {
    /**
     * Poster URL.
     */
    posterUrl?: string
    /**
     * Card title.
     */
    title?: string
    /**
     * Text to display under the title.
     */
    text?: string
    /**
     * Number displayed in the top-right corner.
     */
    topRight?: number
    /**
     * Icon displayed in the top-right corner. Takes precedence over `topRight`.
     */
    topRightIcon?: string
    /**
     * Icon displayed in the bottom-left corner.
     */
    quickActionIcon?: string
    /**
     * Props to pass to the menu icon element.
     */
    quickActionProps?: Record<string, unknown>
    /**
     * Icon displayed in the bottom-right corner.
     */
    menuIcon?: string
    /**
     * Props to pass to the menu icon element.
     */
    menuProps?: object
    progressPercent?: number
  }
>()

const emit = defineEmits<
  ItemCardEmits & {
    clickFab: []
    clickQuickAction: []
    clickMenu: []
    cardLongPress: []
  }
>()

const isHovering = ref(false)

const isPreSelect = computed(() => preSelect && !selected && !disableSelection)
const showSelection = computed(
  () => !disableSelection && (isHovering.value || isPreSelect.value || selected),
)
const showQuickAction = computed(
  () => (isHovering.value || isTouchPrimary.value) && !selected && !preSelect,
)
const showMenu = computed(
  () => isHovering.value && !selected && !preSelect && !isTouchPrimary.value,
)
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

.plain {
  opacity: 0.62;
}
</style>
